package net.sourceforge.mayfly.evaluation.select;

import static net.sourceforge.mayfly.util.MayflyAssert.assertColumn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.MayflyException;

import org.junit.Ignore;
import org.junit.Test;


public class StoreEvaluatorTest {

    /* Is the test right? Is this a property of tables or of joins, FROM, etc?
     * A column name becomes non-ambiguous if that column isn't mentioned
     * in this query, right?
     * Seems like we usually implement this kind of thing with a dummy row.
     */
    @Test
    @Ignore
    public void lookupName() {
        Database database = new Database();
        database.execute("create table foo(a integer, b integer)");
        database.execute("create table bar(b integer)");
        Evaluator evaluator = new StoreEvaluator(database.dataStore());

        assertColumn("foo", "a", evaluator.lookupName("a"));
        try {
            evaluator.lookupName("b");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("ambiguous column b", e.getMessage());
        }
        assertNull(evaluator.lookupName("nosuch"));
    }

}
