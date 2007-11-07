package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;


public class Not extends Condition {

    public final Condition operand;

    public Not(Condition operand) {
        this.operand = operand;
    }

    @Override
    public boolean evaluate(ResultRow row, Evaluator evaluator) {
        return !operand.evaluate(row, evaluator);
    }

    @Override
    public String firstAggregate() {
        return operand.firstAggregate();
    }
    
    @Override
    public void check(ResultRow row) {
        operand.check(row);
    }

}
