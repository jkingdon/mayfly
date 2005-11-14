package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

public class TableIdentifierTest extends TestCase {
    public void testEquals() throws Exception {
        assertEquals(new TableIdentifier("foo"), new TableIdentifier("foo"));
        assertEquals(new TableIdentifier("foo"), new TableIdentifier("FOO"));

        assertFalse(new TableIdentifier("foo").equals(new TableIdentifier("Bar")));
    }
}