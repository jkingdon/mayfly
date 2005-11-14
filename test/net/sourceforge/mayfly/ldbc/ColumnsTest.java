package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;

public class ColumnsTest extends TestCase {
    public void testFromColumnNames() throws Exception {
        assertEquals(
            new Columns(
                new L()
                    .append(new Column("foo", "a"))
                    .append(new Column("foo", "b"))
                    .asImmutable()),
            Columns.fromColumnNames(
                "foo",
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

    public void testColumnMatching() throws Exception {
        Column a = new Column(new TableIdentifier("foo"), "a");
        Column b = new Column(new TableIdentifier("foo"), "b");
        Column c = new Column(new TableIdentifier("bar"), "c");

        Columns columns = new Columns(
            new L()
                .append(a)
                .append(b)
                .append(c)
                .asImmutable()
        );

        assertTrue(new Columns.ColumnMatching("foo", "a").evaluate(a));
        assertFalse(new Columns.ColumnMatching("foo", "a").evaluate(b));

        assertTrue(new Columns.ColumnMatching("FoO", "A").evaluate(a));

        assertTrue(new Columns.ColumnMatching("FoO", "b").evaluate(b));
        assertTrue(new Columns.ColumnMatching("bar", "C").evaluate(c));

        assertFalse(new Columns.ColumnMatching("bar", "C").evaluate(a));
    }


}