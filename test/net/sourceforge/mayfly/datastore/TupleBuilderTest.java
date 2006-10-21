package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.util.MayflyAssert;

public class TupleBuilderTest extends TestCase {
    
    public void testBasics() throws Exception {
        Row builtRow = new TupleBuilder()
            .appendColumnCellContents("a", "val")
            .asRow();
        assertEquals(1, builtRow.columnCount());
        assertEquals("a", builtRow.columnName(0));
        MayflyAssert.assertString("val", builtRow.cell("a"));
    }

}
