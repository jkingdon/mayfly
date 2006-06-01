package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

public class TupleBuilderTest extends TestCase {
    
    public void testBasics() throws Exception {
        assertEquals(
            new Row(new TupleElement(new Column("a"), new StringCell("val"))),
            new TupleBuilder()
                .appendColumnCellContents("a", "val")
                .asRow()
        );
    }

}
