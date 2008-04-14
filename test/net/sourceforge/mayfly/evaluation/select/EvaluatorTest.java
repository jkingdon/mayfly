package net.sourceforge.mayfly.evaluation.select;

import static net.sourceforge.mayfly.util.MayflyAssert.assertColumn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.ResultRow;

import org.junit.Test;


public class EvaluatorTest {

    @Test
    public void lookupName() {
        ResultRow dummyRow = new ResultRow()
            .withColumn("foo", "a", NullCell.INSTANCE)
            .withColumn("foo", "b", NullCell.INSTANCE)
            .withColumn("bar", "b", NullCell.INSTANCE)
        ;
        Evaluator evaluator = Evaluator.NO_SUBSELECT_NEEDED;

        assertColumn("foo", "a", evaluator.lookupName(dummyRow, "a"));
        try {
            evaluator.lookupName(dummyRow, "b");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("ambiguous column b", e.getMessage());
        }
        assertNull(evaluator.lookupName(dummyRow, "nosuch"));
    }

}
