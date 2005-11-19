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

}
