package net.sourceforge.mayfly.acceptance;

import java.math.BigDecimal;
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

    // Here's a small sample of other types we don't test for yet:
    // INT (synonym for INTEGER), CHARACTER VARYING (synonym for VARCHAR)
    //NUMERIC and DECIMAL
    //REAL, FLOAT, DOUBLE - precision can be given in binary digits (24 or 53, typically)
    // BIT and BIT VARYING; BOOLEAN
    // BIGSERIAL (see AutoIncrementTest for the auto-increment syntaxes we do look for)
    // DATE, TIME, TIMESTAMP
    // TIME WITH TIME ZONE is questionable (see postgres docs)
    // BLOB/CLOB
    
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

    private void checkType(boolean expectType, String typeName, String sampleValue) throws SQLException {
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
        if (!dialect.wishThisWereTrue()) {
            return;
        }

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

        /* results.getBigDecimal with a scale intentionally not tested as it is deprecated
           (might want to look at what it was supposed to do and why it was decprecated, though).
         */

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

}
