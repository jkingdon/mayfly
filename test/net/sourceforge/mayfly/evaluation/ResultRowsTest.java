package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.ldbc.where.Equal;

public class ResultRowsTest extends TestCase {
    
    public void testSelect() throws Exception {
        ResultRows rows = new ResultRows()
            .with(new ResultRow().with(new SingleColumn("x"), new LongCell(7)))
            .with(new ResultRow().with(new SingleColumn("x"), new LongCell(9)))
            ;
        assertEquals(2, rows.size());
        
        ResultRows someRows = rows.select(new Equal(new IntegerLiteral(9), new SingleColumn("x")));
        assertEquals(1, someRows.size());
    }

}
