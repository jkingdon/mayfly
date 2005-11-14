package net.sourceforge.mayfly.datastore;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class TuplesTest extends TestCase {

    public void testHeaderIs() throws Exception {
        assertTrue(new Tuples.HeaderIs(new Column("colA")).evaluate(new Tuple(new Column("colA"), new Cell("a"))));
        assertFalse(new Tuples.HeaderIs(new Column("colB")).evaluate(new Tuple(new Column("colA"), new Cell("a"))));
    }

    public void testGetHeader() throws Exception {
        assertEquals(new Column("colA"), new Tuples.GetHeader().transform(new Tuple(new Column("colA"), new Cell("a"))));
    }

    public void testGetCell() throws Exception {
        assertEquals(new Cell("a"), new Tuples.GetCell().transform(new Tuple(new Column("colA"), new Cell("a"))));
    }
}