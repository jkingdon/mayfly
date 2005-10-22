package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

public class CellTest extends TestCase {
    public void testAsInt() throws Exception {
        assertEquals(6, new Cell(new Long(6)).asInt());
        assertEquals(6, new Cell(new Integer(6)).asInt());
    }

    public void testAsLong() throws Exception {
        assertEquals(6L, new Cell(new Long(6)).asLong());
        assertEquals(6L, new Cell(new Integer(6)).asLong());
    }
}