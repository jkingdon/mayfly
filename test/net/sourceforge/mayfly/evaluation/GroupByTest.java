package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.ldbc.Rows;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.util.ImmutableList;

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
        
        Rows sevenActual = grouped.getRows(new GroupByCells(new LongCell(7)));
        Rows sevenExpected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(50))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(51))
                ))
        );
        assertEquals(sevenExpected, sevenActual);

        Rows eightActual = grouped.getRows(new GroupByCells(new LongCell(8)));
        Rows eightExpected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(8))
                    .appendColumnCell("b", new LongCell(52))
                ))
        );
        assertEquals(eightExpected, eightActual);
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
        
        Rows seven50Actual = grouped.getRows(new GroupByCells(new LongCell(7), new LongCell(50)));
        Rows seven50Expected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(50))
                    .appendColumnCell("c", new LongCell(400))
                ))
        );
        assertEquals(seven50Expected, seven50Actual);

        Rows seven51Actual = grouped.getRows(new GroupByCells(new LongCell(7), new LongCell(51)));
        Rows seven51Expected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(7))
                    .appendColumnCell("b", new LongCell(51))
                    .appendColumnCell("c", new LongCell(400))
                ))
        );
        assertEquals(seven51Expected, seven51Actual);
    }

}
