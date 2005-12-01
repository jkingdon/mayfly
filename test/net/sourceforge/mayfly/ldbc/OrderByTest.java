package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.what.*;

public class OrderByTest extends TestCase {

    public void testIsEmpty() throws Exception {
        assertTrue(new OrderBy().isEmpty());
        assertFalse(new OrderBy().add(new SingleColumn("a")).isEmpty());
    }

}
