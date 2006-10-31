package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import org.joda.time.DateTimeZone;

import net.sourceforge.mayfly.util.MayflyAssert;

public class TimestampCellTest extends TestCase {

    private static final long ONE_MINUTE = 60000L;

    public void testAsBriefString() throws Exception {
        assertEquals("1776-07-02 00:00:00", 
            new TimestampCell(1776, 7, 2, 0, 0, 0).asBriefString());
        assertEquals("0532-12-31 23:07:59", 
            new TimestampCell(532, 12, 31, 23, 7, 59).asBriefString());
    }
    
    public void testDisplayName() throws Exception {
        assertEquals("timestamp 2100-07-02 07:02:00", 
            new TimestampCell(2100, 7, 2, 7, 2, 0).displayName());
    }
    
    public void testAsTimestamp() throws Exception {
        long march1 = 636249600000L;
        assertEquals(march1,
            new TimestampCell(1990, 3, 1, 0, 0, 0)
                .asTimestamp(DateTimeZone.UTC).getTime());
        assertEquals(march1 - (5 * 60 * ONE_MINUTE + 30 * ONE_MINUTE),
            new TimestampCell(1990, 3, 1, 0, 0, 0)
                .asTimestamp(DateTimeZone.forOffsetHoursMinutes(5, 30)).getTime());
    }
    
    public void testCompare() throws Exception {
        MayflyAssert.assertIsEquals(
            new TimestampCell(1990, 3, 1, 5, 43, 2), 
            new TimestampCell(1990, 3, 1, 5, 43, 2));
        MayflyAssert.assertLessThan(
            new TimestampCell(1988, 2, 29, 6, 44, 21),
            new TimestampCell(1988, 3, 1, 5, 43, 2));
        MayflyAssert.assertLessThan(
            NullCell.INSTANCE, new TimestampCell(1988, 3, 1, 0, 0, 0));
    }

}
