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
        Columns columns = Columns.fromColumnNames(
                new L()
                    .append("a")
                    .append("b")
            );
        assertEquals(2, columns.columnCount());
        assertEquals("a", columns.column(0).columnName());
        assertEquals("b", columns.column(1).columnName());
    }

    public void testLookup() throws Exception {
        Columns columns = new Columns(new ImmutableList(Arrays.asList(
            new Column[] {
                new Column("a"),
                new Column("a"),
                new Column("b"),
                new Column("d")
            })));
        
        assertEquals("b", columns.columnFromName("b").columnName());
        assertEquals("b", columns.columnFromName("B").columnName() );

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
        
        assertEquals("d", columns.columnFromName("d").columnName());
    }
    
    public void testReplace() throws Exception {
        Columns columns = new Columns(new ImmutableList(Arrays.asList(
            new Column[] {
                new Column("a"),
                new Column("a"),
                new Column("b"),
            })));
        Columns newColumns = columns.replace(
            new Column("b", new LongCell(42), null, true,
                new DefaultDataType(), false)
        );
        
        assertFalse(columns.columnFromName("b").isAutoIncrement());
        assertTrue(newColumns.columnFromName("b").isAutoIncrement());
    }

}
