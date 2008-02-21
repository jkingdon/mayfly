package net.sourceforge.mayfly.acceptance;

import junitx.framework.ArrayAssert;
import junitx.framework.ObjectAssert;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @internal
 * See also:
 * {@link StringTest}
 * {@link DateTest}
 */
public class DataTypeTest extends SqlTestCase {

    public void testTextType() throws Exception {
        checkType(dialect.haveTextType(), "text", "'some text'");
    }

    public void testTinyint() throws Exception {
        checkType(dialect.haveTinyint(), "tinyint", "127");
    }

    public void testSmallint() throws Exception {
        checkType(true, "smallint", "32767");
    }

    public void testInt() throws Exception {
        // A synonym for INTEGER.  Specified by SQL92.
        checkType(true, "Int", "2147483647");
    }

    // Here's a small sample of other types we don't test for yet:
    //NUMERIC
    //REAL, FLOAT, DOUBLE - precision can be given in binary digits (24 or 53, typically)
    // BIT and BIT VARYING; BOOLEAN
    // BIGSERIAL (see AutoIncrementTest for the auto-increment syntaxes we do look for)
    // BLOB/CLOB
    //   sorting and comparison (binary for BLOB, Unicode-based or some such for CLOB)
    //   JDBC CLOB for a TEXT column
    
    /* Here are some we don't test for (or have in Mayfly), but which
       I'm not sure are actually used enough to warrant the clutter:
       CHARACTER VARYING (synonym for VARCHAR)
       CHARACTER (non-VARYING) - this one seems to be the source of many
         implementation headaches (trailing spaces and such), and I'm
         not sure there is any good reason to prefer it over VARCHAR.
       */
    
    public void testInteger() throws Exception {
        execute("create table foo (waist integer, inseam integer)");
        execute("insert into foo (waist, inseam) values (30, 32)");

        {
            ResultSet results = query("select waist, inseam from foo");
            assertTrue(results.next());
            assertEquals(30, results.getInt(1));
            assertEquals(32, results.getInt("inseam"));
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select waist, inseam from foo");
            assertTrue(results.next());
            // Are these supposed to be Integer? Long? Hypersonic says Integer
            assertEquals(30, ((Number) results.getObject(1)).intValue());
            assertEquals(32, ((Number) results.getObject("inseam")).intValue());
            assertFalse(results.next());
            results.close();
        }
    }
    
    public void testGetObject() throws Exception {
        execute("create table foo(x " +
                (dialect.haveTinyint() ? "tinyint" : "smallint") +
                ", y smallint, z integer, w bigint)");
        execute("insert into foo(x, y, z, w) " +
            "values (127, 32767, -2147483648, 222111333444)");
        execute("insert into foo(x, y, z, w) " +
            "values (0, 70, 5, 62)");
        ResultSet results = query("select x,y,z,w,y+z,z+w from foo");
        assertTrue(results.next());
        assertTypesOfRow(results);
        assertTrue(results.next());
        /* The interesting part about this is that all tested databases,
           so far, base the returned type on the types in the expressions,
           not the particular values from this execution. */
        assertTypesOfRow(results);
        assertFalse(results.next());
    }

    private void assertTypesOfRow(ResultSet results) throws SQLException {
        ObjectAssert.assertInstanceOf(
            dialect.typeOfInteger(), results.getObject("x"));
        ObjectAssert.assertInstanceOf(
            dialect.typeOfInteger(), results.getObject("y"));
        ObjectAssert.assertInstanceOf(
            dialect.typeOfInteger(), results.getObject("z"));
        ObjectAssert.assertInstanceOf(Long.class, results.getObject("w"));
        ObjectAssert.assertInstanceOf(
            dialect.expressionsAreTypeLong() ? Long.class : Integer.class, 
            results.getObject(5));
        ObjectAssert.assertInstanceOf(
            dialect.typeFromAddingLongs(), results.getObject(6));
    }
    
    public void testLongDoesNotFit() throws Exception {
        execute("create table foo (x bigint)");
        // larger than 2^32
        execute("insert into foo(x) values (222111333444)");

        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        assertEquals(222111333444L, results.getLong(1));

        try {
            results.getInt(1);
            fail();
        }
        catch (SQLException e) {
            assertMessage("Value 222111333444 does not fit in an int", e);
        }

        try {
            results.getShort(1);
            fail();
        }
        catch (SQLException e) {
            assertMessage("Value 222111333444 does not fit in a short", e);
        }

        try {
            results.getByte(1);
            fail();
        }
        catch (SQLException e) {
            assertMessage("Value 222111333444 does not fit in a byte", e);
        }

        assertFalse(results.next());
        results.close();
    }

    public void testLongWouldFit() throws Exception {
        execute("create table foo (x bigint)");
        execute("insert into foo(x) values (42)");

        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        assertEquals(42L, results.getLong(1));
        assertEquals(42, results.getInt(1));
        assertEquals((short)42, results.getShort(1));
        assertEquals((byte)42, results.getByte(1));

        assertFalse(results.next());
        results.close();
    }
    
    public void testSetShort() throws Exception {
        execute("create table foo(x bigint)");
        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        insert.setByte(1, Byte.MIN_VALUE);
        insert.executeUpdate();
        
        insert.setShort(1, Short.MIN_VALUE);
        insert.executeUpdate();
        
        insert.setInt(1, Integer.MIN_VALUE);
        insert.executeUpdate();
        
        insert.setLong(1, Long.MIN_VALUE);
        insert.executeUpdate();
        
        assertResultSet(
            new String[] { " -128 ", " -32768", 
                " -2147483648 ", " -9223372036854775808 " }, 
            query("select x from foo"));
    }

    private void checkType(boolean expectType, String typeName, String sampleValue) 
    throws SQLException {
        String sql = "create table foo (a " + typeName + ")";
        if (expectType) {
            execute(sql);
            execute("insert into foo(a) values(" + sampleValue + ")");
            assertResultSet(new String[] { sampleValue }, query("select a from foo"));
        }
        else {
            expectExecuteFailure(sql, "expected data type but got " + typeName);
        }
    }
    
    public void testHexInteger() throws Exception {
        execute("create table foo(a integer)");
        String hexForInteger = "insert into foo(a) values(x'a0')";
        if (dialect.allowHexForInteger()) {
            execute(hexForInteger);
            execute("insert into foo(a) values(x'ff')");
            execute("insert into foo(a) values(x'3fff0000')");

            expectExecuteFailure("insert into foo(a) values(x'7')", 
                "hex constant 7 must have an even number of digits");
            expectExecuteFailure("insert into foo(a) values(x'7ff')",
                "hex constant 7ff must have an even number of digits");
            expectExecuteFailure("insert into foo(a) values(x'0g')",
                "invalid character g in hex constant 0g");
            
            assertResultSet(new String[] { "160", "255", "1073676288" },
                query("select a from foo"));
        }
        else {
            expectExecuteFailure(hexForInteger, 
                "attempt to store binary data into integer column a");
        }
    }

    public void testDecimal() throws Exception {
        execute("create table foo (price decimal(4, 2), list_price decimal(5, 2))");
        execute("insert into foo (price, list_price) values (95.0, 99.95)");

        {
            ResultSet results = query("select price, list_price from foo");
            assertTrue(results.next());

            checkDecimal(9500, dialect.decimalScaleIsFromType() ? 2 : 1, 
                results.getBigDecimal(1));
            checkDecimal(9995, 2, results.getBigDecimal("list_price"));

            assertFalse(results.next());

            results.close();
        }

        /* results.getBigDecimal with a scale intentionally not tested 
           as it is deprecated */

        {
            ResultSet results = query("select price, list_price from foo");
            assertTrue(results.next());

            checkDecimal(9500, dialect.decimalScaleIsFromType() ? 2 : 1, 
                (BigDecimal) results.getObject(1));
            checkDecimal(9995, 2, (BigDecimal) results.getObject("list_price"));

            assertFalse(results.next());
            results.close();
        }
    }

    private void checkDecimal(int expectedCents, int expectedScale, 
        BigDecimal actual) {
        assertEquals(expectedCents, actual.movePointRight(2).intValue());
        assertEquals(expectedScale, actual.scale());
    }
    
    public void testIntegerToFromDecimalColumn() throws Exception {
        execute("create table foo(price decimal(4,2))");
        execute("insert into foo(price) values(5)");
        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(price) values(?)");
        statement.setInt(1, 77);
        statement.executeUpdate();

        ResultSet results = query("select price from foo order by price");

        assertTrue(results.next());
        assertEquals(5, results.getInt(1));
        checkDecimal(500, dialect.decimalScaleIsFromType() ? 2 : 0, 
            results.getBigDecimal(1));

        assertTrue(results.next());
        assertEquals(77, results.getInt(1));
        assertFalse(results.next());
    }

    public void testSetDecimal() throws Exception {
        execute("create table foo (price decimal(4, 2), y decimal(11,3))");
        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(price, y) values(?, ?)");
        statement.setBigDecimal(1, new BigDecimal("5.95"));
        statement.setBigDecimal(2, new BigDecimal("197.952").negate());
        statement.executeUpdate();
        
        ResultSet results = query("select price, y from foo");
        assertTrue(results.next());
        assertEquals(5.95, results.getDouble(1), 0.000001);
        assertEquals(-197.952, results.getDouble(2), 0.000001);
        assertFalse(results.next());
    }
    
    public void testSetDouble() throws Exception {
        execute("create table foo (factor decimal(4, 2))");
        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(factor) values(?)");
        statement.setDouble(1, 3.14);
        statement.executeUpdate();
        statement.setDouble(1, -0.03);
        statement.executeUpdate();
        
        ResultSet results = query("select factor from foo");
        assertTrue(results.next());
        assertEquals(314, 
            results.getBigDecimal(1).movePointRight(2).intValue());
        assertTrue(results.next());
        assertEquals(-3, 
            results.getBigDecimal(1).movePointRight(2).intValue());
        assertFalse(results.next());
    }
    
    public void testNullInDecimalColumn() throws Exception {
        execute("create table foo(x decimal(7, 1))");
        execute("insert into foo(x) values(null)");
        assertResultSet(new String[] { " null " }, 
            query("select x from foo"));
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());
        BigDecimal decimal = results.getBigDecimal("x");
        assertNull(decimal);
        assertTrue(results.wasNull());
        assertFalse(results.next());
        results.close();
    }
    
    public void testStringColumnAsNumber() throws Exception {
        execute("create table foo(x varchar(50))");
        execute("insert into foo(x) values('not decimal')");
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());
        try {
            results.getByte("x");
            fail();
        }
        catch (SQLException e) {
            assertMessage(
                "attempt to read string 'not decimal' as a byte", e);
        }

        try {
            results.getShort("x");
            fail();
        }
        catch (SQLException e) {
            assertMessage(
                "attempt to read string 'not decimal' as a short", e);
        }

        try {
            results.getInt("x");
            fail();
        }
        catch (SQLException e) {
            assertMessage(
                "attempt to read string 'not decimal' as an int", e);
        }

        try {
            results.getLong("x");
            fail();
        }
        catch (SQLException e) {
            assertMessage(
                "attempt to read string 'not decimal' as a long", e);
        }

        try {
            results.getDouble("x");
            fail();
        }
        catch (SQLException e) {
            assertMessage(
                "attempt to read string 'not decimal' as a double", e);
        }

        try {
            results.getBigDecimal("x");
            fail();
        }
        catch (SQLException e) {
            assertMessage(
                "attempt to read string 'not decimal' as a decimal", e);
        }
        assertFalse(results.next());
    }
    
    public void testCompareStringWithInteger() throws Exception {
        execute("create table foo(x integer, y varchar(255))");
        execute("insert into foo(x, y) values (5, 'hello')");

        String integerColumnStringLiteral = "select y from foo where x < 'zzz'";
        if (dialect.dataTypesAreEnforced()) {
            expectQueryFailure(integerColumnStringLiteral,
                "attempt to compare string 'zzz' to number 5");
        }
        else {
            /* The obvious question here is what kind of comparison is
               done - string or literal.  But we don't test that. */
            query(integerColumnStringLiteral);
        }

        String stringColumnIntegerLiteral = "select y from foo where y < 99";
        if (dialect.canMixStringAndInteger()) {
            /* The obvious question here is what kind of comparison is
               done - string or literal.  But we don't test that. */
            query(stringColumnIntegerLiteral);
        }
        else {
            expectQueryFailure(stringColumnIntegerLiteral,
            "attempt to compare number 99 to string 'hello'");
        }
    }
    
    public void testIntegerToFromStringColumn() throws Exception {
        execute("create table foo(x varchar(255))");
        
        String insertInteger = "insert into foo(x) values(9)";
        if (dialect.canMixStringAndInteger()) {
            execute(insertInteger);
        }
        else {
            expectExecuteFailure(insertInteger, 
                "attempt to store number 9 into string column x");
            execute("insert into foo(x) values('9')");
        }

        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(x) values(?)");
        statement.setInt(1, 10);
        if (dialect.canSetIntegerOnStringColumn()) {
            statement.executeUpdate();
        }
        else {
            try {
                statement.executeUpdate();
                fail();
            }
            catch (SQLException e) {
                assertMessage(
                    "attempt to store number 10 into string column x", e);
            }
            execute("insert into foo(x) values('10')");
        }
    
        ResultSet results = query("select x from foo order by x");
    
        assertTrue(results.next());
        if (dialect.expectMayflyBehavior()) {
            try {
                results.getInt(1);
                fail();
            }
            catch (SQLException e) {
                assertMessage("attempt to read string '10' as an int", e);
            }
            assertEquals("10", results.getString(1));
            assertTrue(results.next());
            assertEquals("9", results.getString(1));
            assertFalse(results.next());
        }
        else {
            assertEquals(10, results.getInt(1));
            assertTrue(results.next());
            assertEquals(9, results.getInt(1));
            assertFalse(results.next());
        }
    
    }

    public void testDecimalToFromStringColumn() throws Exception {
        execute("create table foo(x varchar(255))");
        
        String insertDecimal = "insert into foo(x) values(9.5)";
        if (dialect.canMixStringAndInteger()) {
            execute(insertDecimal);
        }
        else {
            expectExecuteFailure(insertDecimal, 
                "attempt to store decimal 9.5 into string column x");
            execute("insert into foo(x) values('9.5')");
        }

        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(x) values(?)");
        statement.setBigDecimal(1, new BigDecimal("10.05"));
        if (dialect.canSetIntegerOnStringColumn()) {
            statement.executeUpdate();
        }
        else {
            try {
                statement.executeUpdate();
                fail();
            }
            catch (SQLException e) {
                assertMessage(
                    "attempt to store decimal 10.05 into string column x", e);
            }
            execute("insert into foo(x) values('10.05')");
        }
    
        ResultSet results = query("select x from foo order by x");
    
        assertTrue(results.next());
        if (dialect.expectMayflyBehavior()) {
            try {
                results.getBigDecimal(1);
                fail();
            }
            catch (SQLException e) {
                assertMessage("attempt to read string '10.05' as a decimal", e);
            }
            assertEquals("10.05", results.getString(1));
            assertTrue(results.next());
            assertEquals("9.5", results.getString(1));
            assertFalse(results.next());
        }
        else {
            checkDecimal(1005, 2, results.getBigDecimal(1));
            assertTrue(results.next());
            checkDecimal(950, 1, results.getBigDecimal(1));
            assertFalse(results.next());
        }
    }

    public void testStringToFromDecimalColumn() throws Exception {
        execute("create table foo(x decimal(10,2))");
        
        String insertString = "insert into foo(x) values('9.5')";
        if (dialect.canMixStringAndInteger()) {
            execute(insertString);
        }
        else {
            expectExecuteFailure(insertString, 
                "attempt to store string '9.5' into decimal column x");
            execute("insert into foo(x) values(9.5)");
        }

        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(x) values(?)");
        statement.setString(1, "10.05");
        if (dialect.canSetStringOnDecimalColumn()) {
            statement.executeUpdate();
        }
        else {
            try {
                statement.executeUpdate();
                fail();
            }
            catch (SQLException e) {
                assertMessage(
                    "attempt to store string '10.05' into decimal column x", e);
            }
            execute("insert into foo(x) values(10.05)");
        }
    
        ResultSet results = query("select x from foo order by x");
    
        assertTrue(results.next());
        if (dialect.expectMayflyBehavior()) {
            try {
                results.getString(1);
                fail();
            }
            catch (SQLException e) {
                assertMessage("attempt to read decimal 9.50 as a string", e);
            }
            checkDecimal(950, 2, results.getBigDecimal(1));
            assertTrue(results.next());
            checkDecimal(1005, 2, results.getBigDecimal(1));
            assertFalse(results.next());
        }
        else {
            assertEquals(
                dialect.decimalScaleIsFromType() ? "9.50" : "9.5", 
                results.getString(1));
            assertTrue(results.next());
            assertEquals("10.05", results.getString(1));
            assertFalse(results.next());
        }
    }

    public void testIntegerToFloat() throws Exception {
        execute("create table foo (x bigint, y smallint)");
        // 4503599627370495 is, I believe, the largest integer value which can be
        // represented exactly in a double.
        execute("insert into foo(x, y) values (4503599627370495, 32767)");
        execute("insert into foo(x, y) values (-4503599627370495, -32767)");
        // Likewise for float:
        execute("insert into foo(x, y) values (8388607, 0)");
        execute("insert into foo(x, y) values (-8388607, 0)");
        
        ResultSet results = query("select x, y from foo");

        assertTrue(results.next());
        assertEquals(32767.0, results.getDouble("y"), 0.00001);
        assertEquals(4503599627370495.0, results.getDouble("x"), 0.00001);
        assertEquals(32767.0f, results.getFloat("y"), 0.00001f);
        /* Comparing as doubles rather than floats 
           better shows that bits are lost
           (although we'd have to pick a different integer(s) to
           delve into exactly how many bits are lost) */ 
        assertEquals(4503599627370495.0, results.getFloat("x"), 1.0);
        
        assertEquals(32767.0, results.getDouble(2), 0.00001);
        assertEquals(32767.0f, results.getFloat(2), 0.00001f);

        assertTrue(results.next());
        assertEquals(- 4503599627370495.0, results.getDouble("x"), 0.00001);
        assertEquals(- 32767.0f, results.getFloat("y"), 0.00001f);

        assertTrue(results.next());
        assertEquals(8388607.0f, results.getFloat("x"), 0.00001f);

        assertTrue(results.next());
        assertEquals(- 8388607.0f, results.getFloat("x"), 0.00001f);

        assertFalse(results.next());
        results.close();
    }
    
    public void testDecimalToFloat() throws Exception {
        execute("create table foo (x decimal(10,3))");
        execute("insert into foo(x) values(53.904)");
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());
        assertEquals(53.904, results.getDouble("x"), 0.000001);
        assertFalse(results.next());
        results.close();
    }
    
    public void testBinaryStream() throws Exception {
        execute("create table foo (x " + dialect.binaryTypeName() + ")");

        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        byte[] data = new byte[] { 0x1, 0x3, (byte)0xff, (byte)0x90 };
        /**
            Requiring the correct length here probably wouldn't be
            as big a deal as in the {@link StringTest#testCharacterStream}
            case, although I guess there are cases (e.g. reading
            from a network stream) in which it could be inconvenient.
        */
        insert.setBinaryStream(1, new ByteArrayInputStream(data), data.length);
        assertEquals(1, insert.executeUpdate());
        insert.close();
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        InputStream stream = results.getBinaryStream(1);
        byte[] contents = IOUtils.toByteArray(stream);
        ArrayAssert.assertEquals(data, contents);
        // We don't realy need to close it; the JDBC javadoc says that
        // the next call to a getter method will close the stream
        // for us.
        stream.close();
        
        byte[] viaBytes = results.getBytes("x");
        ArrayAssert.assertEquals(data, viaBytes);

        assertFalse(results.next());
        results.close();
    }
    
    public void testGetBlob() throws Exception {
        execute("create table foo (x " + dialect.binaryTypeName() + ")");

        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        byte[] data = new byte[] { 0x1, 0x3, (byte)0xff, (byte)0x90 };
        /**
            Requiring the correct length here probably wouldn't be
            as big a deal as in the {@link StringTest#testCharacterStream}
            case, although I guess there are cases (e.g. reading
            from a network stream) in which it could be inconvenient.
        */
        insert.setBinaryStream(1, new ByteArrayInputStream(data), data.length);
        // TODO: probably want to test setBlob while we're at it.
//        insert.setBlob(1, blob);
        assertEquals(1, insert.executeUpdate());
        insert.close();
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        if (dialect.blobTypeWorks()) {
            Blob blob = results.getBlob(1);
            assertEquals(4L, blob.length());
            InputStream stream = blob.getBinaryStream();
            byte[] contents = IOUtils.toByteArray(stream);
            ArrayAssert.assertEquals(data, contents);
            // Do we need to close this?
            stream.close();
        }
        else {
            try {
                results.getBlob(1);
                fail("Maybe postgres fixed their Blob bug?");
            }
            catch (SQLException expected) {
            }
        }
        
        assertFalse(results.next());
        results.close();
    }
    
    public void testNumberAsBinary() throws Exception {
        execute("create table foo(x integer)");
        execute("insert into foo(x) values(1)");
        ResultSet results = query("select x from foo");
        assertTrue(results.next());
        if (!dialect.canGetBytesOnNumber()) {
            try {
                results.getBytes(1);
                fail();
            }
            catch (SQLException e) {
                assertMessage("attempt to read number 1 as binary data", e);
            }
        }
        else {
            byte[] bytes = results.getBytes(1);
            // 49 is the value of the character '1'.
            ArrayAssert.assertEquals(new byte[] { 49 }, bytes);
        }
    }

    public void testNonBinaryInBinaryColumn() throws Exception {
        execute("create table foo(x " + dialect.binaryTypeName() + ")");
        String insertOne = "insert into foo(x) values(1)";
        if (dialect.dataTypesAreEnforced()) {
            expectExecuteFailure(insertOne,
                "attempt to store number 1 into binary column x");
            assertResultSet(new String[] { }, query("select x from foo"));
        }
        else {
            execute(insertOne);
            assertResultSet(new String[] { " 1 " }, query("select x from foo"));
        }
    }
    
    public void testHexBinary() throws Exception {
        /* TODO: Also should accept x'00' '01' '02' syntax which allows
           a long hex literal to be continued over several lines */
        execute("create table foo(x " + dialect.binaryTypeName() + ")");
        String hexForBinary = "insert into foo(x) values(x'00010203ff7f00')";
        if (dialect.allowHexForBinary()) {
            execute(hexForBinary);
            execute("insert into foo(x) values (X'00')");

            /** See {@link net.sourceforge.mayfly.parser.LexerTest#testHexErrors()}
               for more tests of these error messages. */
            expectExecuteFailure("insert into foo(x) values(x'7ff')",
                "hex constant x'7ff' must have an even number of digits");
            expectExecuteFailure("insert into foo(x) values(x'0g')",
                "invalid character 'g' in hex constant");
            
            ResultSet results = query("select x from foo");
            assertTrue(results.next());
            ArrayAssert.assertEquals(
                new byte[] { 0, 1, 2, 3, (byte)0xff, (byte)0x7f, 0 }, 
                results.getBytes(1));
            assertTrue(results.next());
            ArrayAssert.assertEquals(
                new byte[] { 0 }, 
                results.getBytes(1));
            assertFalse(results.next());
        }
        else {
            expectExecuteFailure(hexForBinary, 
                "expected expression but got x'00010203ff7f00'");
        }
    }
    
    public void testNullInBinaryColumn() throws Exception {
        execute("create table foo(x " + dialect.binaryTypeName() + ")");
        execute("insert into foo(x) values(null)");
        assertResultSet(new String[] { " null " }, 
            query("select x from foo"));
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());
        byte[] bytes = results.getBytes("x");
        assertNull(bytes);
        assertTrue(results.wasNull());
        assertFalse(results.next());
        results.close();
    }
    
    public void xtestBinaryAsPrimaryKey() throws Exception {
        /* Derby: this is an error because comparisons are not
           supported (could also check ORDER BY or <) */
        execute("create table foo(x " + dialect.binaryTypeName() + 
            " primary key)");
        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        insert.setBytes(1, new byte[] { 0x1, 0x3, (byte)0xff, (byte)0x90 });
        assertEquals(1, insert.executeUpdate());
        insert.setBytes(1, new byte[] { 0x1 });
        assertEquals(1, insert.executeUpdate());
        insert.close();
    }

}
