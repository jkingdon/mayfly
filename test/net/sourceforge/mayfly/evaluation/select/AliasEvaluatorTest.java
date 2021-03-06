package net.sourceforge.mayfly.evaluation.select;

import static net.sourceforge.mayfly.util.MayflyAssert.assertAliasedColumn;
import static net.sourceforge.mayfly.util.MayflyAssert.assertColumn;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.what.AliasedExpression;
import net.sourceforge.mayfly.evaluation.what.What;

public class AliasEvaluatorTest extends TestCase {

    public void testLookUpAlias() throws Exception {
        What what = new What(
            new AliasedExpression("john_smith", new SingleColumn("b"))
        );
        Evaluator evaluator = new AliasEvaluator(what);
        ResultRow row = new ResultRow()
            .withColumn("foo", "a", new LongCell(6))
            .withColumn("foo", "b", new LongCell(7))
            .withColumn("bar", "john_smith", new LongCell(8));

        check(6, "a", evaluator, row);
        check(7, "b", evaluator, row);
        check(7, "john_smith", evaluator, row);
        
        try {
            check(787, "nonexist", evaluator, row);
            fail();
        }
        catch (NoColumn e) {
            assertEquals("no column nonexist", e.getMessage());
        }
    }

    public void testTableNames() throws Exception {
        What what = new What(
            new AliasedExpression("john_smith", new SingleColumn("foo", "b"))
        );
        Evaluator evaluator = new AliasEvaluator(what);
        ResultRow row = new ResultRow()
            .withColumn("foo", "b", new LongCell(7))
            .withColumn("bar", "john_smith", new LongCell(8))
            .withColumn("bar", "b", new LongCell(9))
            ;

        try {
            check(7, null, "b", evaluator, row);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("ambiguous column b", e.getMessage());
        }

        check(7, null, "john_smith", evaluator, row);
        check(8, "bar", "john_smith", evaluator, row);
        check(9, "bar", "b", evaluator, row);
    }

    private void check(long expected, String column, Evaluator evaluator, ResultRow row) {
        check(expected, null, column, evaluator, row);
    }

    private void check(long expected, String table, String column, 
        Evaluator evaluator, ResultRow row) {
        LongCell value = (LongCell) evaluator.lookup(row, table, table, column, null);
        assertEquals(expected, value.asLong());
    }
    
    public void testLookupName() throws Exception {
        What what = new What(
            new AliasedExpression("john_smith", new SingleColumn("b"))
        );
        ResultRow dummyRow = new ResultRow()
            .withColumn("foo", "a", NullCell.INSTANCE)
            .withColumn("foo", "b", NullCell.INSTANCE)
            .withColumn("bar", "john_smith", NullCell.INSTANCE)
        ;
        Evaluator evaluator = 
            new AliasEvaluator(what, Evaluator.NO_SUBSELECT_NEEDED);

        // Not yet implemented (see StoreEvaluatorTest):
        assertColumn("foo", "a", evaluator.lookupName(dummyRow, "a"));
        assertColumn("foo", "b", evaluator.lookupName(dummyRow, "b"));
        assertAliasedColumn("john_smith", null, "b", 
            evaluator.lookupName(dummyRow, "john_smith"));
        assertNull(evaluator.lookupName(dummyRow, "nonexist"));
    }

}
