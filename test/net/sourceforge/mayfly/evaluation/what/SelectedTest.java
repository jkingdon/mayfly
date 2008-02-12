package net.sourceforge.mayfly.evaluation.what;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.GroupByCells;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.util.MayflyAssert;

public class SelectedTest extends TestCase {
    
    public void testEvaluateAll() throws Exception {
        Selected selected = new Selected(
            new Plus(new SingleColumn("x"), new SingleColumn("y")));
        ResultRow row = new ResultRow()
            .with(new SingleColumn("x"), new LongCell(7))
            .with(new SingleColumn("y"), new LongCell(11))
        ;
        GroupByCells cells = selected.evaluateAll(row);
        assertEquals(1, cells.size());
        MayflyAssert.assertLong(18, cells.get(0));
    }
    
    public void testToRow() throws Exception {
        Selected selected = new Selected(
            new Plus(new SingleColumn("x"), new SingleColumn("y")));
        GroupByCells cells = new GroupByCells(new LongCell(18));
        
        ResultRow row = selected.toRow(cells);
        assertEquals(1, row.size());

        Plus expression = (Plus) row.expression(0);
        MayflyAssert.assertColumn("x", expression.left());
        MayflyAssert.assertColumn("y", expression.right());
        
        MayflyAssert.assertLong(18, row.cell(0));
    }
    
}
