package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.what.Selected;

/**
 * @internal
 * Implementations of this class should be immutable (or have no state at
 * all).
 */
public abstract class Distinct {
    
    abstract public ResultRows distinct(Selected selected, ResultRows rows);

}
