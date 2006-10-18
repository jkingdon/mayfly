package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;

public class True extends Condition {
    public boolean evaluate(ResultRow candidate) {
        return true;
    }

    public String firstAggregate() {
        return null;
    }
    
    public void check(ResultRow row) {
    }

}
