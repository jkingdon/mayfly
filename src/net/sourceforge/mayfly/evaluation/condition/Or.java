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
    public Condition resolve(ResultRow row, Evaluator evaluator) {
        Condition newLeftSide = leftSide.resolve(row, evaluator);
        Condition newRightSide = rightSide.resolve(row, evaluator);
        if (newLeftSide != leftSide || newRightSide != rightSide) {
            return new Or(newLeftSide, newRightSide);
        }
        else {
            return this;
        }
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
