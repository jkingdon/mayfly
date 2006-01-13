package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

public class CellTest extends TestCase {
    public void testAsInt() throws Exception {
        assertEquals(6, new LongCell(6).asInt());
        assertEquals(6, Cell.fromContents(new Integer(6)).asInt());
        assertEquals(6, Cell.fromContents(new Long(6)).asInt());
    }

    public void testAsLong() throws Exception {
        assertEquals(6L, new LongCell(6).asLong());
    }

    public void testAsString() throws Exception {
        assertEquals("a", new StringCell("a").asString());
    }
    
    public void testCompare() throws Exception {
        assertComparesEqual(new LongCell(6), new LongCell(6));
        assertLessThan(new LongCell(6), new LongCell(7));
        
        assertComparesEqual(new StringCell("foo"), new StringCell("foo"));
        assertLessThan(new StringCell("11"), new StringCell("5"));

        assertComparesEqual(NullCell.INSTANCE, NullCell.INSTANCE);
        assertLessThan(NullCell.INSTANCE, new StringCell(""));
        assertLessThan(NullCell.INSTANCE, new LongCell(0));
    }

    private void assertComparesEqual(Cell first, Cell second) {
        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
        
        // I think the GROUP BY code makes more sense if
        // compareTo is consistent with equals()
        assertEquals(first, second);
        assertEquals(second, first);
    }

    private void assertLessThan(Cell first, Cell second) {
        {
            int comparison = first.compareTo(second);
            assertTrue("expected <0 but was " + comparison, comparison < 0);
        }

        {
            int comparison = second.compareTo(first);
            assertTrue("expected >0 but was " + comparison, comparison > 0);
        }
    }
    
    public void testDisplayName() throws Exception {
        assertEquals("string 'foo'", new StringCell("foo").displayName());
        assertEquals("string 'don''t'", new StringCell("don't").displayName());
        assertEquals("number -5", new LongCell(-5).displayName());
        assertEquals("null value", NullCell.INSTANCE.displayName());
    }

}
