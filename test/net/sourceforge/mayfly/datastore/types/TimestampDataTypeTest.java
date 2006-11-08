package net.sourceforge.mayfly.datastore.types;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TimestampCell;
import net.sourceforge.mayfly.evaluation.Value;

public class TimestampDataTypeTest extends TestCase {
    
    public void testCoerce() throws Exception {
        Value input = new Value(new StringCell("2038-12-01 03:43:07"));
        TimestampCell cell = (TimestampCell) 
            new TimestampDataType().coerce(input);
        assertEquals(2038, cell.year());
    }
    
    public void testCallsGenericCoerce() throws Exception {
        Cell coerced = 
            new TimestampDataType().coerce(new Value(NullCell.INSTANCE));
        ObjectAssert.assertInstanceOf(NullCell.class, coerced);
    }
    
    public void testCoerceFromTimestamp() throws Exception {
        TimestampCell in = new TimestampCell(2003, 1, 7, 13, 45, 00);
        TimestampCell coerced = (TimestampCell) 
            new TimestampDataType().coerce(new Value(in));
        assertEquals(13, coerced.hour());
    }

    public void testStringToDate() throws Exception {
        checkFailure("");
        checkFailure("123");
        checkFailure("12345");
        checkFailure("12345-01-01 00:00:00");
        checkFailure("x1234-01-01 00:00:00");
        checkFailure("1234-01-01 00:00:00x");
        check(1234, 1, 1, 0, 0, 0, "1234-01-01 00:00:00");
        checkFailure("1066");
        checkFailure("1066x12-25");
        checkFailure("106612-25");
        check(1066, 12, 25, 0, 0, 0, "1066-12-25");
        checkFailure("1066-12-25T00:00:00"); // Should allow this?
        checkFailure("1066-12-25 0:00:00");
        checkFailure("1066-12-25 00:00:0");
        check(1066, 12, 25, 23, 59, 43, "1066-12-25 23:59:43");
        try {
            new TimestampDataType().stringToDate("1890-02-31 00:00:01");
            fail();
        }
        catch (org.joda.time.IllegalFieldValueException expected) {
            // I'm thinking it is probably a mistake for Mayfly
            // to wrap the exception just to hide the fact that
            // mayfly is implemented using Joda.  Is it important
            // that it get caught by the "catch SQLException" which
            // tends to wrap most people's JDBC calls?
        }
    }

    public void testQuotingInMessage() throws Exception {
        try {
            new TimestampDataType().stringToDate("don't");
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "'don''t' is not in format yyyy-mm-dd hh:mm:ss", 
                e.getMessage());
        }
    }

    private void check(int expectedYear, int expectedMonth, int expectedDay,
        int expectedHour, int expectedMinute, int expectedSecond,
        String input) {
        TimestampCell stamp = new TimestampDataType().stringToDate(input);
        assertEquals(expectedYear, stamp.year());
        assertEquals(expectedMonth, stamp.month());
        assertEquals(expectedDay, stamp.day());
        assertEquals(expectedHour, stamp.hour());
        assertEquals(expectedMinute, stamp.minute());
        assertEquals(expectedSecond, stamp.second());
    }

    private void checkFailure(String input) {
        try {
            new TimestampDataType().stringToDate(input);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("'" +
                input +
                "' is not in format yyyy-mm-dd hh:mm:ss", e.getMessage());
        }
    }

}
