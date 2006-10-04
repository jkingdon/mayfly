package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.util.ImmutableList;

public class TableDataTest extends TestCase {
    
    public void testDropColumn() throws Exception {
        Column a = new Column("a");
        Column b = new Column("b");
        Columns columns = new Columns(
            ImmutableList.fromArray(new Column[] { a, b }));
        Row row = new TupleBuilder()
            .append(a, new LongCell(7))
            .append(b, new StringCell("hi"))
            .asRow();
        Rows rows = new Rows(row);
        TableData table = new TableData(
            columns, new Constraints(), rows);
        
        TableData newTable = table.dropColumn("B");
        
        assertEquals(1, newTable.columns().size());
        
        assertEquals(1, newTable.rowCount());
        Row newRow = (Row) newTable.rows().element(0);
        assertEquals(1, newRow.size());
        TupleElement element = (TupleElement) newRow.element(0);
        assertEquals("a", element.column().columnName());
    }

}
