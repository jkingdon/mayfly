package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;
import static net.sourceforge.mayfly.util.MayflyAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;


public class RowEvaluatorTest {
    
    @Test
    public void lookup() {
        Evaluator nested = Evaluator.NO_SUBSELECT_NEEDED;
        ResultRow correlatedRow = new ResultRow().withColumn("aaa", "a", new LongCell(7));
        RowEvaluator evaluator = new RowEvaluator(correlatedRow, nested);

        ResultRow rowFromInner = new ResultRow().withColumn("bbb", "b", new LongCell(8));
        assertLong(7, evaluator.lookup(rowFromInner, "aaa", null, "a", Location.UNKNOWN));
        assertLong(7, evaluator.lookup(rowFromInner, null, null, "a", Location.UNKNOWN));
        assertLong(8, evaluator.lookup(rowFromInner, "bbb", null, "b", Location.UNKNOWN));
        assertLong(8, evaluator.lookup(rowFromInner, null, null, "b", Location.UNKNOWN));
        checkLookupFails(rowFromInner, evaluator, null, "c", "c");
        checkLookupFails(rowFromInner, evaluator, "ccc", "c", "ccc.c");
        checkLookupFails(rowFromInner, evaluator, "aaa", "b", "aaa.b");
        checkLookupFails(rowFromInner, evaluator, "bbb", "a", "bbb.a");
    }

    private void checkLookupFails(
        ResultRow row, RowEvaluator evaluator, String table, String column,
        String expectedDisplayName) {
        try {
            evaluator.lookup(row, table, table, column, Location.UNKNOWN);
            fail();
        }
        catch (NoColumn expected) {
            assertEquals(expectedDisplayName, expected.displayName());
        }
    }
    
    @Test
    public void lookupName() {
        Evaluator nested = Evaluator.NO_SUBSELECT_NEEDED;
        ResultRow nonCorrelatedRow = new ResultRow().withColumn("bbb", "b", new LongCell(8));
        ResultRow correlatedRow = new ResultRow().withColumn("aaa", "a", new LongCell(7));
        RowEvaluator evaluator = new RowEvaluator(correlatedRow, nested);

        assertColumn("aaa", "a", evaluator.lookupName(nonCorrelatedRow, "a"));
        assertColumn("bbb", "b", evaluator.lookupName(nonCorrelatedRow, "b"));
        assertNull(evaluator.lookupName(nonCorrelatedRow, "c"));
    }

}
