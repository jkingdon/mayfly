package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

public class ColumnTest extends TestCase {

    public void testMatches() throws Exception {
        assertTrue(new Column("aaa").matches("aAa"));
        assertFalse(new Column("aaa").matches("aaB"));
    }

}
