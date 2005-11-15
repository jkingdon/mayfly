package net.sourceforge.mayfly.datastore;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.what.*;

public class TupleBuilderTest extends TestCase {
    
    public void testBasics() throws Exception {
        assertEquals(
            new Tuple(new TupleElement(new Column("a"), new Cell("val"))),
            new TupleBuilder()
                .appendColumnCellTuple("a", "val")
                .asTuple()
        );
    }

}
