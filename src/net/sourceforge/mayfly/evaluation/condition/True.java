package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class True extends Condition {

    public boolean evaluate(ResultRow candidate, Evaluator evaluator) {
        return true;
    }

    public String firstAggregate() {
        return null;
    }
    
    public void check(ResultRow row) {
    }

}
