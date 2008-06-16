package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.expression.Concatenate;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.MayflyAssert;

public class GroupByKeysTest extends TestCase {
    
    public void testResolveColumns() throws Exception {
        GroupByKeys keys = new GroupByKeys(
            new GroupItem(new Concatenate(new SingleColumn("a"), new QuotedString("'abc'"))));
        ResultRow row = new ResultRow().withColumn("foo", "a", NullCell.INSTANCE);
        keys = keys.resolve(row, Evaluator.NO_SUBSELECT_NEEDED);
        
        assertEquals(1, keys.size());
        Expression expression = keys.get(0).expression();
        Concatenate concatenate = (Concatenate) expression;
        SingleColumn column = (SingleColumn) concatenate.left();
        MayflyAssert.assertColumn("foo", "a", column);
        QuotedString string = (QuotedString) concatenate.right();
        assertEquals(new QuotedString("'abc'"), string);
    }
    
    // TODO: max(a) aggregateexpression
    // TODO: count(*) (does nothing)
    // TODO: error case (ambiguous column reference)

}
