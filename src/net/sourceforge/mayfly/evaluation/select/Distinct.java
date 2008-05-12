package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.what.Selected;

public abstract class Distinct {
    
    abstract public ResultRows distinct(Selected selected, ResultRows rows);

}
