package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;


public class Not extends Condition {

    public final Condition operand;

    public Not(Condition operand) {
        this.operand = operand;
    }

    public boolean evaluate(ResultRow row) {
        return !operand.evaluate(row);
    }

    public String firstAggregate() {
        return operand.firstAggregate();
    }
    
    public void check(ResultRow row) {
        operand.check(row);
    }

}
