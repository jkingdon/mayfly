package net.sourceforge.mayfly.acceptance;

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

    public void testNull() throws Exception {
        execute("create table foo (start_date date, end_date date)");
        execute("insert into foo (start_date) values (null)");
        assertResultSet(new String[] { " null, null " }, 
            query("select start_date, end_date from foo"));
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
    
    public void testCurentTimestamp() throws Exception {
        execute("create table foo (x timestamp default current_timestamp)");
    }

}
