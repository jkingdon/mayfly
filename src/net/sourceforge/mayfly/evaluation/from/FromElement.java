package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

/**
 * @internal
 * Table reference or join.  All implementors should be immutable objects.
 */
public abstract class FromElement {

    public abstract ResultRows tableContents(Evaluator evaluator);

    public abstract ResultRow dummyRow(Evaluator evaluator);

}
