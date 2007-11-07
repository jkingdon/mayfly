package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class Or extends Condition {

    public final Condition leftSide;
    public final Condition rightSide;

    public Or(Condition leftSide, Condition rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    @Override
    public boolean evaluate(ResultRow row, Evaluator evaluator) {
        return leftSide.evaluate(row, evaluator) || 
            rightSide.evaluate(row, evaluator);
    }

    @Override
    public String firstAggregate() {
        return firstAggregate(leftSide, rightSide);
    }
    
    @Override
    public void check(ResultRow row) {
        leftSide.check(row);
        rightSide.check(row);
    }

}
