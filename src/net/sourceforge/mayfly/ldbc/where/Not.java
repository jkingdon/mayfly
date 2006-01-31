package net.sourceforge.mayfly.ldbc.where;


public class Not extends BooleanExpression {

    private final BooleanExpression operand;

    public Not(BooleanExpression operand) {
        this.operand = operand;
    }

    public boolean evaluate(Object row) {
        return !operand.evaluate(row);
    }

    public String firstAggregate() {
        return operand.firstAggregate();
    }

}
