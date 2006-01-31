package net.sourceforge.mayfly.ldbc.where;


public class And extends BooleanExpression {
    private BooleanExpression leftSide;
    private BooleanExpression rightSide;

    public And(BooleanExpression leftSide, BooleanExpression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object row) {
        return leftSide.evaluate(row) && rightSide.evaluate(row);
    }

    public String firstAggregate() {
        return firstAggregate(leftSide, rightSide);
    }

}
