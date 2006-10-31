package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import org.joda.time.DateTimeZone;

public class DateCellTest extends TestCase {
    
    public void testAsBriefString() throws Exception {
        assertEquals("1776-07-02", new DateCell(1776, 7, 2).asBriefString());
        assertEquals("0532-12-31", new DateCell(532, 12, 31).asBriefString());
    }
    
    public void testAsDate() throws Exception {
        long march1 = 636249600000L;
        assertEquals(march1,
            new DateCell(1990, 3, 1).asDate(DateTimeZone.UTC).getTime());
    }
    
    public void testCompare() throws Exception {
        assertIsEquals(new DateCell(1990, 3, 1), new DateCell(1990, 3, 1));
        assertLessThan(new DateCell(1988, 2, 29), new DateCell(1988, 3, 1));
        assertLessThan(NullCell.INSTANCE, new DateCell(1988, 3, 1));
    }

    private void assertLessThan(Cell cell1, Cell cell2) {
        assertFalse(cell1.sqlEquals(cell2));
        assertFalse(cell2.sqlEquals(cell1));
        assertTrue(cell1.compareTo(cell2) < 0);
        assertTrue(cell2.compareTo(cell1) > 0);
    }

    private void assertIsEquals(Cell cell1, Cell cell2) {
        assertTrue(cell1.sqlEquals(cell2));
        assertTrue(cell2.sqlEquals(cell1));
        assertEquals(0, cell1.compareTo(cell2));
        assertEquals(0, cell2.compareTo(cell1));
    }

}
