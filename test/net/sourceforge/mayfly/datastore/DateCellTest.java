package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.TimeZone;

public class DateCellTest extends TestCase {
    
    public void testAsBriefString() throws Exception {
        assertEquals("1776-07-02", new DateCell(1776, 7, 2).asBriefString());
        assertEquals("0532-12-31", new DateCell(532, 12, 31).asBriefString());
    }
    
    public void testAsDate() throws Exception {
        Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long march1 = 636249600000L;
        assertEquals(march1,
            new DateCell(1990, 3, 1).asDate(gmt).getTime());
    }

}
