package net.sourceforge.mayfly.acceptance;

import junitx.framework.ArrayAssert;

import net.sourceforge.mayfly.EndToEndTests;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataTypeTest extends SqlTestCase {

    public void testStrings() throws Exception {
        execute("create table foo (color varchar(80), size varchar(80))");
        execute("insert into foo (color, size) values ('red', 'medium')");

        {
            ResultSet results = query("select size, color from foo");
            assertTrue(results.next());
            assertEquals("medium", results.getString(1));
            assertEquals("red", results.getString("color"));
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select size, color from foo");
            assertTrue(results.next());
            assertEquals("medium", results.getObject(1));
            assertEquals("red", results.getObject("color"));
            assertFalse(results.next());
            results.close();
        }
    }

    public void testAsciiPunctuation() throws Exception {
        execute("create table foo (value varchar(255))");
        execute("insert into foo (value) values (' !\"#$%&''()*+,-./:;<=>?@[]^_`{|}~')");

        ResultSet results = query("select value from foo where value = ' !\"#$%&''()*+,-./:;<=>?@[]^_`{|}~'");
        assertTrue(results.next());
        assertEquals(" !\"#$%&'()*+,-./:;<=>?@[]^_`{|}~", results.getString(1));
        assertFalse(results.next());
    }
    
    public void testBackSlash() throws Exception {
        execute("create table foo (value varchar(255))");

        String insertSql = "insert into foo (value) values ('\\')";
        String selectSql = "select value from foo where value = '\\'";

        if (dialect.backslashInAStringIsAnEscape()) {
            expectExecuteFailure(insertSql, "unterminated string literal");
            expectExecuteFailure(selectSql, "unterminated string literal");
        }
        else {
            execute(insertSql);
    
            ResultSet results = query(selectSql);
            assertTrue(results.next());
            assertEquals("\\", results.getString(1));
            assertFalse(results.next());
        }
    }
    
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
    // CHARACTER VARYING (synonym for VARCHAR)
    //NUMERIC
    //REAL, FLOAT, DOUBLE - precision can be given in binary digits (24 or 53, typically)
    // BIT and BIT VARYING; BOOLEAN
    // BIGSERIAL (see AutoIncrementTest for the auto-increment syntaxes we do look for)
    // BLOB/CLOB
    //   sorting and comparison (binary for BLOB, Unicode-based or some such for CLOB)
    //   JDBC CLOB for a TEXT column
    
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

    public void testDecimal() throws Exception {
        execute("create table foo (price decimal(4, 2), list_price decimal(5, 2))");
        execute("insert into foo (price, list_price) values (95.0, 99.95)");

        {
            ResultSet results = query("select price, list_price from foo");
            assertTrue(results.next());
            assertEquals(9500, results.getBigDecimal(1).movePointRight(2).intValue());
            assertEquals(9995, results.getBigDecimal("list_price").movePointRight(2).intValue());
            assertFalse(results.next());
            results.close();
        }

        /* results.getBigDecimal with a scale intentionally not tested as it is deprecated */

        {
            ResultSet results = query("select price, list_price from foo");
            assertTrue(results.next());

            BigDecimal price = (BigDecimal) results.getObject(1);
            assertEquals(9500, price.movePointRight(2).intValue());
            assertEquals(dialect.decimalScaleIsFromType() ? 2 : 1, price.scale());

            BigDecimal listPrice = (BigDecimal) results.getObject("list_price");
            assertEquals(9995, listPrice.movePointRight(2).intValue());
            assertEquals(2, listPrice.scale());

            assertFalse(results.next());
            results.close();
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
    
    /**
     * @internal
     * Also see {@link EndToEndTests#testCharacterStream()}
     * which checks a few more cases.
     */
    public void testCharacterStream() throws Exception {
        execute("create table foo (x varchar(255))");

        PreparedStatement insert = 
            connection.prepareStatement("insert into foo(x) values(?)");
        String data = "value";
        // Derby requires that the correct length be passed in.  That is bogus,
        // because there is no way to get that length when reading from, say, a
        // UTF-8 file, short of reading the whole file.  MySQL, Postgres and Hypersonic
        // do fine with a length of "1000".
        insert.setCharacterStream(1, new StringReader(data), data.length());
        assertEquals(1, insert.executeUpdate());
        insert.close();
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        Reader stream = results.getCharacterStream(1);
        String contents = IOUtils.toString(stream);
        assertEquals("value", contents);
        stream.close(); // Check JDBC documentation: should I close it?

        assertFalse(results.next());
        results.close();
    }
    
    public void xtestBinaryLiteral() throws Exception {
        // Is there a syntax for this?
        // Do we care?
        execute("create table foo (x blob(255))");
        execute("insert into foo(x) values(1 || 2 || 255)");
        ResultSet results = query("select x from foo");
        assertTrue(results.next());

        InputStream stream = results.getBinaryStream(1);
        byte[] contents = IOUtils.toByteArray(stream);
        ArrayAssert.assertEquals(new byte[] {1, 2, (byte) 255}, contents);
        stream.close(); // Check JDBC documentation: should I close it?

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
            as big a deal as in the {@link #testCharacterStream}
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
                "attempt to store number 1 as binary data");
            assertResultSet(new String[] { }, query("select x from foo"));
        }
        else {
            execute(insertOne);
            assertResultSet(new String[] { " 1 " }, query("select x from foo"));
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

}
