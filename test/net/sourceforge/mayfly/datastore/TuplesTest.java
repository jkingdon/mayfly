package net.sourceforge.mayfly.datastore;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class TuplesTest extends TestCase {

    public void testHeaderIs() throws Exception {
        assertTrue(new Tuple.HeaderIs(new Column("colA")).evaluate(new TupleElement(new Column("colA"), new Cell("a"))));
        assertFalse(new Tuple.HeaderIs(new Column("colB")).evaluate(new TupleElement(new Column("colA"), new Cell("a"))));
    }

    public void testGetHeader() throws Exception {
        assertEquals(new Column("colA"), new Tuple.GetHeader().transform(new TupleElement(new Column("colA"), new Cell("a"))));
    }

    public void testGetCell() throws Exception {
        assertEquals(new Cell("a"), new Tuple.GetCell().transform(new TupleElement(new Column("colA"), new Cell("a"))));
    }
}