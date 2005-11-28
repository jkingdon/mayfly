package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import java.util.*;

import net.sourceforge.mayfly.*;
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

    public void testColumnMatching() throws Exception {
        Column a = new Column(new TableIdentifier("foo"), "a");
        Column b = new Column(new TableIdentifier("foo"), "b");
        Column c = new Column(new TableIdentifier("bar"), "c");

        assertTrue(new Columns.ColumnMatching("foo", "a").evaluate(a));
        assertFalse(new Columns.ColumnMatching("foo", "a").evaluate(b));

        assertTrue(new Columns.ColumnMatching("FoO", "A").evaluate(a));

        assertTrue(new Columns.ColumnMatching("FoO", "b").evaluate(b));
        assertTrue(new Columns.ColumnMatching("bar", "C").evaluate(c));

        assertFalse(new Columns.ColumnMatching("bar", "C").evaluate(a));
    }
    
    public void testLookup() throws Exception {
        Columns columns = new Columns(new ImmutableList(Arrays.asList(
            new Column[] {
                new Column("foo", "a"),
                new Column("bar", "a"),
                new Column("foo", "b"),
                new Column("d")
            })));
        
        assertEquals(new Column("foo", "b"), columns.columnFromName("b"));
        assertEquals(new Column("foo", "b"), columns.columnFromName("B"));

        try {
            columns.columnFromName("c");
            fail();
        } catch (MayflyException e) {
            assertEquals("no column c", e.getMessage());
        }

        try {
            columns.columnFromName("a");
            fail();
        } catch (MayflyException e) {
            assertEquals("ambiguous column a", e.getMessage());
        }
        
        assertEquals(new Column("d"), columns.columnFromName("d"));

    }


}