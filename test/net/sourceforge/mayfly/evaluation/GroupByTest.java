package net.sourceforge.mayfly.evaluation;

import junit.framework.*;

import java.util.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

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

        Iterator iterator = grouped.keyIterator();
        assertEquals(new LongCell(7), iterator.next());
        assertEquals(new LongCell(8), iterator.next());
        assertFalse(iterator.hasNext());
        
        Rows sevenActual = grouped.getRows(new LongCell(7));
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

        Rows eightActual = grouped.getRows(new LongCell(8));
        Rows eightExpected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("a", new LongCell(8))
                    .appendColumnCell("b", new LongCell(52))
                ))
        );
        assertEquals(eightExpected, eightActual);
    }

}
