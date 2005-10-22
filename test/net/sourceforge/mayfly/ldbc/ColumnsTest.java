package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class ColumnsTest extends TestCase {
    public void testFromColumnNames() throws Exception {
        assertEquals(
            new Columns(
                new L()
                    .append(new Column("a"))
                    .append(new Column("b"))),
            Columns.fromColumnNames(
                new L()
                    .append("a")
                    .append("b")
            )
        );
    }

    public void testAsNames() throws Exception {
        assertEquals("a", new Columns.ToName().transform(new Column("a")));
    }

    public void testHasEquivalentName() throws Exception {
        assertTrue(new Columns.HasEquivalentName("a").evaluate(new Column("a")));
        assertFalse(new Columns.HasEquivalentName("a").evaluate(new Column("b")));
    }
}