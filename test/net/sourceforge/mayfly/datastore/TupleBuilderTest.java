package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.util.MayflyAssert;

public class TupleBuilderTest extends TestCase {
    
    public void testBasics() throws Exception {
        Row builtRow = new TupleBuilder()
            .append("a", "val")
            .asRow();
        assertEquals(1, builtRow.columnCount());
        MayflyAssert.assertString("val", builtRow.cell("a"));
    }

}
