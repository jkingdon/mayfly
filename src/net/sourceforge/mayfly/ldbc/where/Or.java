package net.sourceforge.mayfly.ldbc.where;


public class Or extends BooleanExpression {

    public final BooleanExpression leftSide;
    public final BooleanExpression rightSide;

    public Or(BooleanExpression leftSide, BooleanExpression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object row) {
        return leftSide.evaluate(row) || rightSide.evaluate(row);
    }

    public String firstAggregate() {
        return firstAggregate(leftSide, rightSide);
    }

}
