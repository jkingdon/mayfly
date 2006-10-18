package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.ResultRow;


public class And extends Condition {
    public final Condition leftSide;
    public final Condition rightSide;

    public And(Condition leftSide, Condition rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(ResultRow row) {
        /*
         * We currently implement this as a short-circuited evaluation.
         * 
         * Apparently in SQL an implementation is allowed to short
         * circuit, but not required to and not forbidden to rearrange
         * the expression.
         * 
         * As for what side effects would make it matter, divide
         * by zero is a classic case.  That's one which depends on
         * the data, rather than a row-independent error like 
         * syntax error, ambiguous column, etc.
         */
        return leftSide.evaluate(row) && rightSide.evaluate(row);
    }

    public String firstAggregate() {
        return firstAggregate(leftSide, rightSide);
    }
    
    public void check(ResultRow row) {
        leftSide.check(row);
        rightSide.check(row);
    }

}
