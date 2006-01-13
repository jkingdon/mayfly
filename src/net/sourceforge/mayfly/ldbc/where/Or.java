package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

public class Or extends BooleanExpression {

    private BooleanExpression leftSide;
    private BooleanExpression rightSide;

    public Or(BooleanExpression leftSide, BooleanExpression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object row) {
        return leftSide.evaluate(row) || rightSide.evaluate(row);
    }

    public int parameterCount() {
        return leftSide.parameterCount() + rightSide.parameterCount();
    }

    public void substitute(Iterator jdbcParameters) {
        leftSide.substitute(jdbcParameters);
        rightSide.substitute(jdbcParameters);
    }

    public String firstAggregate() {
        return firstAggregate(leftSide, rightSide);
    }

}
