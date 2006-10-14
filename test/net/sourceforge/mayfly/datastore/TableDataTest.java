package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.datastore.types.DateDataType;
import net.sourceforge.mayfly.datastore.types.DefaultDataType;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.UpdateTable;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Collections;

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
        
        TableData newTable = table.dropColumn(null, "B");
        
        assertEquals(1, newTable.columns().size());
        
        assertEquals(1, newTable.rowCount());
        Row newRow = (Row) newTable.rows().element(0);
        assertEquals(1, newRow.size());
        TupleElement element = (TupleElement) newRow.element(0);
        assertEquals("a", element.column().columnName());
    }
    
    public void testModifyColumn() throws Exception {
        Column a = new Column("foo", "a", NullCell.INSTANCE, 
            null, false, new DefaultDataType(), true);
        Row row = new TupleBuilder()
            .append(a, new LongCell(7))
            .asRow();
        TableData table = new TableData(
            Columns.singleton(a), new Constraints(), new Rows(row));
        
        Column newA = new Column("foo", "a", NullCell.INSTANCE, 
            null, false, new DefaultDataType(), false);
        TableData newTable = table.modifyColumn(newA);

        Column foundColumn = newTable.findColumn("a");
        assertFalse(foundColumn.isNotNull);
    }

    public void testCoerceOnUpdate() throws Exception {
        Column a = new Column("foo", "a", NullCell.INSTANCE, 
            null, false, new DateDataType(), false);
        Row row = new TupleBuilder()
            .append(a, new LongCell(7))
            .asRow();
        TableData table = new TableData(
            Columns.singleton(a), new Constraints(), new Rows(row));
        
        UpdateTable update = table.update(new NullChecker(), 
            Collections.singletonList(
                new SetClause("a", new QuotedString("'2004-02-29'"))), 
            BooleanExpression.TRUE);
        assertEquals(1, update.rowsAffected());
        TableData newTable = update.table();
        Row newRow = (Row) newTable.rows().element(0);
        DateCell cell = (DateCell) newRow.cell(null, "a");
        assertEquals(2004, cell.year());
        assertEquals(2, cell.month());
        assertEquals(29, cell.day());
    }

}