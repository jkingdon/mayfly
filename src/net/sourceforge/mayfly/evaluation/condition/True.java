package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class True extends Condition {

    @Override
    public boolean evaluate(ResultRow candidate, Evaluator evaluator) {
        return true;
    }
    
    @Override
    public Condition resolve(ResultRow row, Evaluator evaluator) {
        return this;
    }

    @Override
    public String firstAggregate() {
        return null;
    }
    
    @Override
    public void check(ResultRow row) {
    }

}
