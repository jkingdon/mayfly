package net.sourceforge.mayfly.acceptance;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

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
    //NUMERIC
    //REAL, FLOAT, DOUBLE - precision can be given in binary digits (24 or 53, typically)
    // BIT and BIT VARYING; BOOLEAN
    // BIGSERIAL (see AutoIncrementTest for the auto-increment syntaxes we do look for)
    // DATE (see testDate), TIME, TIMESTAMP
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
    
    private static final long ONE_SECOND = 1000L;
    private static final long ONE_MINUTE = ONE_SECOND * 60L;
    private static final long ONE_HOUR = ONE_MINUTE * 60L;
    private static final long ONE_DAY = ONE_HOUR * 24L;

    private static final long NOVEMBER_27_UTC = 1069891200000L;
    private static final long NOVEMBER_29_UTC = NOVEMBER_27_UTC + 2 * ONE_DAY;

    public void testDatePassUtcCalendar() throws Exception {
        execute("create table foo (start_date date, end_date date)");
        execute("insert into foo (start_date, end_date) " +
            "values ('2003-11-27', '2003-11-29')");

        {
            Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            ResultSet results = query("select start_date, end_date from foo");
            assertTrue(results.next());

            assertDate(NOVEMBER_27_UTC, results.getDate(1, utc).getTime());
            assertDate(NOVEMBER_29_UTC, results.getDate("end_date", utc).getTime());

            assertFalse(results.next());
            results.close();
        }
        
        // TODO: without a calendar.
        // TODO: setDate, calendar and non-calendar
        // TODO: getObject
        // TODO: getDate on non-date
        // TODO: default '2004-07-27'
        // TODO: SQL92 literal syntax: date '2003-11-27' as in
        //execute("insert into foo (start_date, end_date) 
        //    values (date '2003-11-27', date '2003-11-29')");
    }

    public void testDatePassNonUtcCalendar() throws Exception {
        execute("create table foo (start_date date, end_date date)");
        execute("insert into foo (start_date, end_date) " +
            "values ('2003-11-27', '2003-11-29')");

        // Like previous test but non-UTC
        Calendar indianTime = 
            Calendar.getInstance(TimeZone.getTimeZone("GMT+0530"));
        ResultSet results = query("select start_date, end_date from foo");
        assertTrue(results.next());

        // Midnight Indian time is 5 1/2 hours earlier than
        // midnight UTC.
        long november27indian = NOVEMBER_27_UTC -
            (5 * ONE_HOUR + 30 * ONE_MINUTE);
        long november29indian = november27indian + 2 * ONE_DAY;
        assertDate(november27indian, 
            results.getDate(1, indianTime).getTime());
        assertDate(november29indian, 
            results.getDate("end_date", indianTime).getTime());

        assertFalse(results.next());
        results.close();
    }
    
    public void xtestTimestamp() throws Exception {
        // Need to figure out what hypersonic is doing
        // with timezones (I think it is just wrong; Derby
        // agrees with what I would expect).
        execute("create table foo (start_time timestamp, end_time timestamp)");
        execute("insert into foo(start_time, end_time) " +
            "values ('2003-11-27 00:00:00', '2003-11-29 00:00:00')");
//    "values ('2003-11-27 01:07:43', '2003-11-29')");

        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        ResultSet results = query("select start_time, end_time from foo");
        assertTrue(results.next());

//        assertEquals(NOVEMBER_27_UTC + ONE_HOUR + 7 * ONE_MINUTE + 43 * ONE_SECOND, 
//            results.getTimestamp(1, utc).getTime());
        System.out.println("Nov 27 is" + NOVEMBER_27_UTC);
        System.out.println("Nov 29 is" + NOVEMBER_29_UTC);
//        System.out.println(new java.util.Date(1069909200000L).toGMTString());
//        assertEquals(NOVEMBER_27_UTC, 
//            results.getTimestamp("start_time", utc).getTime());
//        assertEquals(NOVEMBER_27_UTC, 
//            results.getTimestamp(1, utc).getTime());
        assertEquals(NOVEMBER_29_UTC, 
            results.getTimestamp("end_time", utc).getTime());

        assertFalse(results.next());
        results.close();
    }

    private void assertDate(long expected, long actual) {
        // Need to figure out what is going on here.  It might be
        // similar to hypersonic and TIMESTAMP.  Needs a closer
        // look, but seems to have something to do with the
        // local time zone.
        if (dialect.datesAreOff()) {
            long oneDayEarlier = expected - ONE_DAY;
            long oneDayLater = expected + ONE_DAY;
            assertTrue("Expected between " + oneDayEarlier + " and " + oneDayLater + " but was " + actual,
                actual >= oneDayEarlier && actual < oneDayLater
            );
        }
        else {
            assertEquals(expected, actual);
        }
    }

}
