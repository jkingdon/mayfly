package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;

/**
 * @internal
 * Implementations of this interface should be immutable.
 */
public interface Aggregator {

    public abstract ResultRows group(
        ResultRows rows, Evaluator evaluator, Selected selected);

    public abstract ResultRow check(
        ResultRow dummyRow, Evaluator evaluator, Selected selected);
    
    public abstract Aggregator resolve(ResultRow dummyRow, Evaluator evaluator);

}
