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
                    .append(new Column("b"))
                    .asImmutable()),
            Columns.fromColumnNames(
                new L()
                    .append("a")
                    .append("b")
            )
        );
    }

    public void testAsNames() throws Exception {
        assertEquals("a", new Columns.ToName().transform(new Column("a")));
        assertEquals("Id", new Columns.ToName().transform(new Column("Id")));
    }

    public void testAsLowercaseNames() throws Exception {
        assertEquals("a", new Columns.ToLowercaseName().transform(new Column("a")));
        assertEquals("id", new Columns.ToLowercaseName().transform(new Column("Id")));
    }

    public void testHasEquivalentName() throws Exception {
        assertTrue(new Columns.HasEquivalentName("a").evaluate(new Column("a")));
        assertTrue(new Columns.HasEquivalentName("a").evaluate(new Column("A")));
        assertTrue(new Columns.HasEquivalentName("a").evaluate(new Column("foo", "a")));
        assertFalse(new Columns.HasEquivalentName("a").evaluate(new Column("b")));
    }
}