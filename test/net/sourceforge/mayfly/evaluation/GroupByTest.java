package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.MayflyAssert;

import java.util.Iterator;

public class GroupByTest extends TestCase {
    
    public void testGroup() throws Exception {
        GroupBy groupBy = new GroupBy();
        groupBy.add(new GroupItem(new SingleColumn("a")));
        Rows rows = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(50))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(51))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(8))
                    .appendColumnCell("b", new LongCell(52))
                ))
        );
        
        GroupedRows grouped = groupBy.makeGroupedRows(rows);
        assertEquals(2, grouped.groupCount());

        Iterator iterator = grouped.iteratorForFirstKeys();
        assertEquals(new LongCell(7), iterator.next());
        assertEquals(new LongCell(8), iterator.next());
        assertFalse(iterator.hasNext());
        
        ResultRows sevenRows = 
            grouped.getRows(new GroupByCells(new LongCell(7)));

        assertEquals(2, sevenRows.size());
        expectRow(7, 50, sevenRows.row(0));
        expectRow(7, 51, sevenRows.row(1));

        ResultRows eightRows =
            grouped.getRows(new GroupByCells(new LongCell(8)))
        ;
        assertEquals(1, eightRows.size());
        expectRow(8, 52, eightRows.row(0));
    }

    private void expectRow(int expectedA, int expectedB, ResultRow fifty) {
        MayflyAssert.assertColumn("a", expectedA, fifty, 0);
        MayflyAssert.assertColumn("b", expectedB, fifty, 1);
    }

    public void testMutiple() throws Exception {
        GroupBy groupBy = new GroupBy();
        groupBy.add(new GroupItem(new SingleColumn("a")));
        groupBy.add(new GroupItem(new SingleColumn("b")));
        Rows rows = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(50))
                    .appendColumnCell("c", new LongCell(400))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(51))
                    .appendColumnCell("c", new LongCell(400))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(8))
                    .appendColumnCell("b", new LongCell(51))
                    .appendColumnCell("c", new LongCell(300))
                ))
        );
        
        GroupedRows grouped = groupBy.makeGroupedRows(rows);
        assertEquals(3, grouped.groupCount());

        Iterator iterator = grouped.iteratorForFirstKeys();
        assertEquals(new LongCell(7), iterator.next());
        assertEquals(new LongCell(7), iterator.next());
        assertEquals(new LongCell(8), iterator.next());
        assertFalse(iterator.hasNext());
        
        ResultRows seven50 = 
            grouped.getRows(new GroupByCells(new LongCell(7), new LongCell(50)))
        ;
        assertEquals(1, seven50.size());
        ResultRow seven50Row = seven50.row(0);
        MayflyAssert.assertColumn("a", 7, seven50Row, 0);
        MayflyAssert.assertColumn("b", 50, seven50Row, 1);
        MayflyAssert.assertColumn("c", 400, seven50Row, 2);

        ResultRows seven51 =
            grouped.getRows(new GroupByCells(new LongCell(7), new LongCell(51)))
        ;
        ResultRow seven51Row = seven51.row(0);
        MayflyAssert.assertColumn("a", 7, seven51Row, 0);
        MayflyAssert.assertColumn("b", 51, seven51Row, 1);
        MayflyAssert.assertColumn("c", 400, seven51Row, 2);
    }

}
