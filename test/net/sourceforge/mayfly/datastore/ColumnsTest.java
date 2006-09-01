package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.types.DefaultDataType;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

import java.util.Arrays;

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
    
    public void testReplace() throws Exception {
        Columns columns = new Columns(new ImmutableList(Arrays.asList(
            new Column[] {
                new Column("foo", "a"),
                new Column("bar", "a"),
                new Column("foo", "b"),
            })));
        Columns newColumns = columns.replace(
            new Column("foo", "b", new LongCell(42), null,
            true, new DefaultDataType()));
        
        assertFalse(columns.columnFromName("b").isAutoIncrement());
        assertTrue(newColumns.columnFromName("b").isAutoIncrement());
    }

}
