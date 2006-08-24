package net.sourceforge.mayfly.datastore.types;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.DateCell;

public class DateDataTypeTest extends TestCase {

    public void testStringToDate() throws Exception {
        checkFailure("");
        checkFailure("123");
        checkFailure("12345");
        checkFailure("12345-01-01");
        checkFailure("x1234-01-01");
        checkFailure("1066");
        checkFailure("1066x12-25");
        checkFailure("106612-25");
        check(1066, 12, 25, "1066-12-25");
        // OK, what's the deal with checking ranges and such?
        check(1890, 2, 31, "1890-02-31");
    }

    public void testQuotingInMessage() throws Exception {
        try {
            new DateDataType().stringToDate("don't");
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "'don''t' is not in format yyyy-mm-dd", e.getMessage());
        }
    }

    private void check(int expectedYear, int expectedMonth, int expectedDay, 
        String input) {
        DateCell date = new DateDataType().stringToDate(input);
        assertEquals(expectedYear, date.year());
        assertEquals(expectedMonth, date.month());
        assertEquals(expectedDay, date.day());
    }

    private void checkFailure(String input) {
        try {
            new DateDataType().stringToDate(input);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("'" +
                    input +
                    "' is not in format yyyy-mm-dd", e.getMessage());
        }
    }

}
