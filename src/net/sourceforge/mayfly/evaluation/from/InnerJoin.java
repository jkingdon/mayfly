package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class InnerJoin extends Join {

    public InnerJoin(FromElement left, FromElement right, Condition condition) {
        super(left, right, condition);
    }

    @Override
    public ResultRows tableContents(Evaluator evaluator) {
        ResultRows unfiltered = 
            left.tableContents(evaluator)
                .join(right.tableContents(evaluator));
        return unfiltered.select(condition, evaluator);
    }
    
    @Override
    public FromElement addToCondition(Condition conditionToAndIn) {
        return new InnerJoin(left, right, condition.makeAnd(conditionToAndIn));
    }

}
