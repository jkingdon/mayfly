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
        assertEquals(0, new LongCell(6).compareTo(new LongCell(6)));
        assertEquals(-1, new LongCell(6).compareTo(new LongCell(7)));
        
        assertEquals(0, new StringCell("foo").compareTo(new StringCell("foo")));
        assertLessThan(new StringCell("11"), new StringCell("5"));
    }

    private void assertLessThan(StringCell first, StringCell second) {
        {
            int comparison = first.compareTo(second);
            assertTrue("expected <0 but was " + comparison, comparison < 0);
        }

        {
            int comparison = second.compareTo(first);
            assertTrue("expected >0 but was " + comparison, comparison > 0);
        }
    }

}
