package net.sourceforge.mayfly.datastore;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class CellHeadersTest extends TestCase {
    

    public void testIsColumn() throws Exception {
        assertFalse(new CellHeaders.IsColumn().evaluate("a"));
        assertTrue(new CellHeaders.IsColumn().evaluate(new Column("colA")));
    }
}