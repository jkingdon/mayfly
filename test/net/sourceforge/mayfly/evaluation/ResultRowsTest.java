package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.condition.Equal;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.util.MayflyAssert;

public class ResultRowsTest extends TestCase {
    
    public void testSelect() throws Exception {
        ResultRows rows = new ResultRows()
            .with(new ResultRow().with(new SingleColumn("x"), new LongCell(7)))
            .with(new ResultRow().with(new SingleColumn("x"), new LongCell(9)))
            ;
        assertEquals(2, rows.rowCount());
        
        ResultRows someRows = rows.select(
            new Equal(new IntegerLiteral(9), new SingleColumn("x")));
        assertEquals(1, someRows.rowCount());
    }

    public void testJoin() throws Exception {
        ResultRows left = new ResultRows()
            .with(new ResultRow().withColumn("foo", "x", new LongCell(7)))
            .with(new ResultRow().withColumn("foo", "x", new LongCell(9)))
            ;
        ResultRows right = new ResultRows()
            .with(new ResultRow().withColumn("bar", "y", new LongCell(10)))
            .with(new ResultRow().withColumn("bar", "y", new LongCell(20)))
            ;
        
        ResultRows joined = left.join(right);

        assertEquals(4, joined.rowCount());
        assertRow(7, 10, joined.row(0));
        assertRow(7, 20, joined.row(1));
        assertRow(9, 10, joined.row(2));
        assertRow(9, 20, joined.row(3));
    }

    private void assertRow(int expectedX, int expectedY, ResultRow row) {
        assertEquals(2, row.size());
        MayflyAssert.assertColumn("foo", "x", expectedX, row, 0);
        MayflyAssert.assertColumn("bar", "y", expectedY, row, 1);
    }

}
