package net.sourceforge.mayfly.datastore;

import junit.framework.*;

public class TupleBuilderTest extends TestCase {
    
    public void testBasics() throws Exception {
        assertEquals(
            new Tuple(new TupleElement(new Column("a"), new StringCell("val"))),
            new TupleBuilder()
                .appendColumnCellContents("a", "val")
                .asTuple()
        );
    }

}
