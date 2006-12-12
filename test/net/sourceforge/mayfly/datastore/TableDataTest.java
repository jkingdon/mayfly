package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.datastore.constraint.PrimaryKey;
import net.sourceforge.mayfly.datastore.constraint.UniqueConstraint;
import net.sourceforge.mayfly.datastore.types.DateDataType;
import net.sourceforge.mayfly.datastore.types.FakeDataType;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.UpdateTable;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.expression.DefaultValue;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.MayflyAssert;

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
        
        assertEquals(1, newTable.columns().columnCount());
        
        assertEquals(1, newTable.rowCount());
        Row newRow = newTable.row(0);
        assertEquals(1, newRow.columnCount());
        assertEquals("a", newRow.columnName(0));
    }
    
    public void testModifyColumn() throws Exception {
        Column a = new Column("a", DefaultValue.NOT_SPECIFIED, null, 
            false, new FakeDataType(), true);
        Row row = new TupleBuilder()
            .append(a, new LongCell(7))
            .asRow();
        TableData table = new TableData(
            Columns.singleton(a), new Constraints(), new Rows(row));
        
        Column newA = new Column("a", DefaultValue.NOT_SPECIFIED, null, 
            false, new FakeDataType(), false);
        TableData newTable = table.modifyColumn(newA);

        Column foundColumn = newTable.findColumn("a");
        assertFalse(foundColumn.isNotNull);
    }

    public void testCoerceOnUpdate() throws Exception {
        Column a = new Column("a", DefaultValue.NOT_SPECIFIED, null, 
            false, new DateDataType(), false);
        Row row = new TupleBuilder()
            .append(a, new LongCell(7))
            .asRow();
        TableData table = new TableData(
            Columns.singleton(a), new Constraints(), new Rows(row));
        
        UpdateTable update = table.update(new NullChecker(), 
            Collections.singletonList(
                new SetClause("a", new QuotedString("'2004-02-29'"))), 
            Condition.TRUE, "table1");
        assertEquals(1, update.rowsAffected());
        TableData newTable = update.table();
        Row newRow = newTable.row(0);
        DateCell cell = (DateCell) newRow.cell("a");
        assertEquals(2004, cell.year());
        assertEquals(2, cell.month());
        assertEquals(29, cell.day());
    }
    
    public void testDelete() throws Exception {
        Column a = new Column("a", DefaultValue.NOT_SPECIFIED, null, 
            false, new FakeDataType(), false);
        Row one = new TupleBuilder()
            .append(a, new LongCell(1))
            .asRow();
        Row two = new TupleBuilder()
            .append(a, new LongCell(2))
            .asRow();
        TableData table = new TableData(
            Columns.singleton(a), 
            new Constraints(), 
            new Rows(ImmutableList.fromArray(new Row[] { one, two }))
        );
        
        Condition where = new Parser("a = 1").parseCondition().asBoolean();
        UpdateTable newTable = table.delete(where, new NullChecker(), "table1");

        assertEquals(1, newTable.rowsAffected());
        assertEquals(1, newTable.table().rowCount());
        Row remainingRow = newTable.table().row(0);
        MayflyAssert.assertLong(2, remainingRow.cell("a"));
    }
    
    public void testHasPrimaryKeyIsSelective() throws Exception {
        TableData table = new TableData(
            Columns.fromColumnNames(
                new L()
                    .append("a")
                    .append("b")
                    .append("c")
            ),
            new Constraints(
                ImmutableList.fromArray(new Constraint[] {
                    new PrimaryKey(Columns.singleton(new Column("a"))),
                    new UniqueConstraint(Columns.singleton(new Column("c")))
                })
            ), 
            new Rows()
        );
        
        assertTrue(table.canBeTargetOfForeignKey("a"));
        assertFalse(table.canBeTargetOfForeignKey("b"));
        assertTrue(table.canBeTargetOfForeignKey("c"));
    }
    
    public void testHasPrimaryKeyWithNone() throws Exception {
        TableData table = new TableData(
            Columns.fromColumnNames(ImmutableList.singleton("a")),
            new Constraints(), 
            new Rows()
        );
        assertFalse(table.canBeTargetOfForeignKey("a"));
    }

}
