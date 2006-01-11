package net.sourceforge.mayfly.datastore;

import junit.framework.*;

public class TupleTest extends TestCase {

    public void testHeaderIs() throws Exception {
        assertTrue(
            new Tuple.HeaderIs(new Column("colA"))
                .evaluate(new TupleElement(new Column("colA"), new StringCell("a")))
        );
        assertFalse(
            new Tuple.HeaderIs(new Column("colB"))
                .evaluate(new TupleElement(new Column("colA"), new StringCell("a")))
        );
    }

    public void testGetHeader() throws Exception {
        assertEquals(
            new Column("colA"),
            new Tuple.GetHeader().transform(new TupleElement(new Column("colA"), new StringCell("a")))
        );
    }

    public void testGetCell() throws Exception {
        assertEquals(
            new StringCell("a"), 
            new Tuple.GetCell().transform(new TupleElement(new Column("colA"), new StringCell("a")))
        );
    }
    
    public void testFindByPosition() throws Exception {
        
    }

}
