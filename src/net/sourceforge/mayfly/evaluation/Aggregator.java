package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;

/**
 * @internal
 * Not yet immutable, because of {@link GroupByKeys}
 */
public interface Aggregator {

    public abstract ResultRows group(
        ResultRows rows, Evaluator evaluator, Selected selected);

    public abstract ResultRow check(
        ResultRow dummyRow, Evaluator evaluator, Selected selected);

}
