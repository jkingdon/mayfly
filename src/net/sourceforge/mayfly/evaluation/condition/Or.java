package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;

public class Or extends Condition {

    public final Condition leftSide;
    public final Condition rightSide;

    public Or(Condition leftSide, Condition rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(ResultRow row) {
        return leftSide.evaluate(row) || rightSide.evaluate(row);
    }

    public String firstAggregate() {
        return firstAggregate(leftSide, rightSide);
    }
    
    public void check(ResultRow row) {
        leftSide.check(row);
        rightSide.check(row);
    }

}
