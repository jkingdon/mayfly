package net.sourceforge.mayfly.evaluation.select;

import junit.framework.TestCase;

import net.sourceforge.mayfly.ldbc.what.SingleColumn;

public class OrderByTest extends TestCase {

    public void testIsEmpty() throws Exception {
        assertTrue(new OrderBy().isEmpty());
        assertFalse(new OrderBy().add(new SingleColumn("a")).isEmpty());
    }

}
