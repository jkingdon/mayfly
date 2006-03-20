package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

public class CellHeadersTest extends TestCase {
    

    public void testIsColumn() throws Exception {
        assertFalse(new CellHeaders.IsColumn().evaluate("a"));
        assertTrue(new CellHeaders.IsColumn().evaluate(new Column("colA")));
    }
}