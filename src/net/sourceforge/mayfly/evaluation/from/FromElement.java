package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

/**
 * @internal
 * Table reference or join.  All implementors should be immutable objects.
 */
public abstract class FromElement {

    public abstract ResultRows tableContents(Evaluator evaluator);

    public ResultRow dummyRow(Evaluator evaluator) {
        return dummyRow(evaluator.store, evaluator.currentSchema);
    }

    public abstract ResultRow dummyRow(DataStore store, String currentSchema);

}
