package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.evaluation.ResultRow;


public class Or extends BooleanExpression {

    public final BooleanExpression leftSide;
    public final BooleanExpression rightSide;

    public Or(BooleanExpression leftSide, BooleanExpression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(ResultRow row) {
        return leftSide.evaluate(row) || rightSide.evaluate(row);
    }

    public String firstAggregate() {
        return firstAggregate(leftSide, rightSide);
    }

}
