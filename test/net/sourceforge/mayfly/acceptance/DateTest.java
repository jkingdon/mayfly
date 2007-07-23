package net.sourceforge.mayfly.acceptance;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.TimeZone;

public class DateTest extends SqlTestCase {

    // DATE (see testDate), TIME, TIMESTAMP
    // TIME WITH TIME ZONE is questionable (see postgres docs)

    private static final long ONE_SECOND = 1000L;
    private static final long ONE_MINUTE = ONE_SECOND * 60L;
    private static final long ONE_HOUR = ONE_MINUTE * 60L;
    private static final long ONE_DAY = ONE_HOUR * 24L;

    /** How long the test might execute between saving an approximate
       expected "current" time and actually reading the current time.
       Normally, a second or a few would tend to suffice, but given the
       possibility of stopping/resuming a test run with SIGSTOP, laptop suspend,
       or similar mechanisms, one could argue
       for values up to days.  An hour seems like a reasonable compromise. */
    private static final long HOW_LONG_A_TEST_MIGHT_TAKE = ONE_HOUR;
    /** If the database server is on another machine, any difference between
       the clocks of the machines would need to be included here.  If the
       database reads the clock with, say, one second granularity and
       the test gets millisecond granularity, then up to a second might need
       to be included (depending on rounding). */
    private static final long HOW_MUCH_CURRENT_TIMESTAMP_MIGHT_BE_IN_THE_PAST =
        ONE_MINUTE;

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

    public void testNull() throws Exception {
        execute("create table foo (start_date date, end_date date)");
        execute("insert into foo (start_date) values (null)");
        
        // In this case we are reading as ints (or strings, I forget)
        assertResultSet(new String[] { " null, null " }, 
            query("select start_date, end_date from foo"));
        
        // More interesting is to read as dates
        ResultSet results = query("select start_date, end_date from foo");
        assertTrue(results.next());
        assertNull(results.getDate("start_date"));
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        assertNull(results.getDate(2, utc));
        assertFalse(results.next());
    }
    
    public void testNullTimestamp() throws Exception {
        execute("create table foo (x timestamp, y timestamp)");
        execute("insert into foo (x) values (null)");

        {
            // Read as strings
            ResultSet results = query("select x, y from foo");
            assertTrue(results.next());

            if (dialect.timestampDoesNotRespectNull()) {
                // insert null seems to be a synonym for 
                // insert CURRENT_TIMESTAMP
                assertTrue(results.getString("x").startsWith("20"));
                assertFalse(results.wasNull());

                /* y is the MySQL zero date.  Trying to read it
                   with getString throws an exception... */
                assertEquals(0, results.getInt(2));
                assertFalse(results.wasNull());
            }
            else {
                assertEquals(null, results.getString("x"));
                assertTrue(results.wasNull());

                assertEquals(null, results.getString(2));
                assertTrue(results.wasNull());
            }
    
            assertFalse(results.next());
        }
        
        if (dialect.timestampDoesNotRespectNull()) {
            return;
        }

        {
            // More interesting is to read as timestamps
            ResultSet results = query("select x, y from foo");
            assertTrue(results.next());
    
            assertEquals(null, results.getTimestamp("x"));
            assertTrue(results.wasNull());
            assertEquals(null, results.getTimestamp(2));
            assertTrue(results.wasNull());
    
            assertFalse(results.next());
        }
    }
    
    public void testGetDateNoCalendar() throws Exception {
        execute("create table foo (start_date date, end_date date)");
        execute("insert into foo (start_date, end_date) " +
            "values ('2003-11-27', '2003-11-29')");

        ResultSet results = query("select start_date, end_date from foo");
        assertTrue(results.next());

        long november27testMachineTimeZone = 
            new DateMidnight(2003, 11, 27).getMillis();
        assertEquals(november27testMachineTimeZone,
            results.getDate(1).getTime());
        assertEquals(november27testMachineTimeZone,
            results.getTimestamp(1).getTime());

        long november29testMachineTimeZone = 
            new DateMidnight(2003, 11, 29).getMillis();
        assertEquals(november29testMachineTimeZone, 
            results.getDate("end_date").getTime());
        assertEquals(november29testMachineTimeZone, 
            results.getTimestamp("end_date").getTime());

        assertFalse(results.next());
        results.close();
    }

    public void xtestTimestampWithCalendar() throws Exception {
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

    public void testTimestampNoCalendar() throws Exception {
        execute("create table foo (start_time timestamp, end_time timestamp)");
        execute("insert into foo(start_time, end_time) " +
            "values ('2003-11-27 01:07:43', '2003-11-29 00:00:00')");

        ResultSet results = query("select start_time, end_time from foo");
        assertTrue(results.next());

        long november27testMachineTimeZone = 
            new DateTime(2003, 11, 27, 1, 7, 43, 000).getMillis();
        assertEquals(november27testMachineTimeZone,
            results.getTimestamp(1).getTime());

        long november29testMachineTimeZone = 
            new DateMidnight(2003, 11, 29).getMillis();
        assertEquals(november29testMachineTimeZone, 
            results.getTimestamp("end_time").getTime());

        assertFalse(results.next());
        results.close();
    }

    private void assertDate(long expected, long actual) {
        assertDateBetween(
            expected - dateFudge(), 
            expected + dateFudge(), 
            actual);
    }

    private void assertDateBetween(
        long startExpected, long endExpected, long actual) {
        assertTrue("Expected between " + startExpected + 
            " and " + endExpected + 
            " but was " + actual,
            actual >= startExpected && actual <= endExpected
        );
    }
    
    public void testCurrentTimestamp() throws Exception {
        execute("create table foo (" +
            "x timestamp default current_timestamp," +
            "y integer" +
            ")");
        execute("insert into foo(y) values(5)");

        long aboutNow = System.currentTimeMillis();
        ResultSet results = query("select x, y from foo");
        assertTrue(results.next());
        assertDateBetween(
            aboutNow 
                - HOW_MUCH_CURRENT_TIMESTAMP_MIGHT_BE_IN_THE_PAST
                - dateFudge(), 
            aboutNow + HOW_LONG_A_TEST_MIGHT_TAKE + dateFudge(),
            results.getTimestamp("x").getTime());
        assertEquals(5, results.getInt("y"));
        assertFalse(results.next());
    }
    
    private long dateFudge() {
        // Need to figure out what is going on here.  It might be
        // similar to hypersonic and TIMESTAMP.  Needs a closer
        // look, but seems to have something to do with the
        // local time zone.
        if (dialect.datesAreOff()) {
            return ONE_DAY;
        }
        else {
            return 0L;
        }
    }

    public void testSetDateOrTimestamp() throws Exception {
        execute("create table foo(x date, y timestamp)");
        PreparedStatement statement = connection.prepareStatement(
            "insert into foo(x, y) values(?, ?)");
        
        long november27testMachineTimeZone = 
            new DateMidnight(2003, 11, 27).getMillis();

        long november29testMachineTimeZone = 
            new DateTime(2003, 11, 29, 1, 7, 43, 000).getMillis();

        statement.setDate(1, new java.sql.Date(november27testMachineTimeZone));
        statement.setTimestamp(2, 
            new java.sql.Timestamp(november29testMachineTimeZone));
        statement.executeUpdate();
        
        ResultSet results = query("select x, y from foo");
        assertTrue(results.next());
        assertEquals(november27testMachineTimeZone,
            results.getDate(1).getTime());
        assertEquals(november29testMachineTimeZone, 
            results.getTimestamp(2).getTime());
        assertFalse(results.next());
    }
    
    public void testDateInTimestampColumn() throws Exception {
        execute("create table foo(x timestamp)");
        String insert = "insert into foo(x) values('2004-03-27')";
        if (dialect.allowDateInTimestampColumn()) {
            execute(insert);
            ResultSet results = query("select x from foo");
            assertTrue(results.next());
            assertEquals(
                new DateMidnight(2004, 3, 27).getMillis(), 
                results.getTimestamp("x").getTime());
            assertFalse(results.next());
        }
        else {
            expectExecuteFailure(insert, 
                "'2004-03-27' is not in format yyyy-mm-dd hh:mm:ss");
        }
    }

    public void testTimestampInDateColumn() throws Exception {
        execute("create table foo(x date)");
        String insert = "insert into foo(x) values('2004-03-27 12:31:00')";
        if (dialect.allowTimestampInDateColumn()) {
            execute(insert);
            ResultSet results = query("select x from foo");
            assertTrue(results.next());
            assertEquals(
                new DateMidnight(2004, 3, 27).getMillis(), 
                results.getDate("x").getTime());
            assertFalse(results.next());
        }
        else {
            expectExecuteFailure(insert, 
                "'2004-03-27 12:31:00' is not in format yyyy-mm-dd");
        }
    }
    
    public void testCompareWithString() throws Exception {
        execute("create table foo(x date)");
        execute("insert into foo(x) values('2003-03-27')");
        execute("insert into foo(x) values('2004-06-11')");
        execute("insert into foo(x) values('2004-01-21')");
        ResultSet results = 
            query("select x from foo where x < '2004-02-29' order by x");

        assertTrue(results.next());
        assertEquals(
            new DateMidnight(2003, 3, 27).getMillis(), 
            results.getDate("x").getTime());

        assertTrue(results.next());
        assertEquals(
            new DateMidnight(2004, 1, 21).getMillis(), 
            results.getDate("x").getTime());

        assertFalse(results.next());
    }
    
    public void testEqualsWithString() throws Exception {
        execute("create table foo(x date, y varchar(255))");
        execute(
            "insert into foo(x, y) values('2004-07-04', 'Independence Day')");
        execute("insert into foo(x, y) values('2004-11-11', 'Armistice Day')");
        
        assertResultSet(new String[] { " 'Armistice Day' " }, 
            query("select y from foo where x = '2004-11-11'"));
        assertResultSet(new String[] { " 'Independence Day' " }, 
            query("select y from foo where x <> '2004-11-11'"));
    }

}
