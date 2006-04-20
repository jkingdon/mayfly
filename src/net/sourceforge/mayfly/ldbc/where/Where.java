package net.sourceforge.mayfly.ldbc.where;


public class Where extends BooleanExpression {

    public static final Where EMPTY = new Where(BooleanExpression.TRUE);

    private final BooleanExpression expression;

    public Where(BooleanExpression expression) {
        this.expression = expression;
    }

    public boolean evaluate(Object candidate) {
        return expression.evaluate(candidate);
    }

    public String firstAggregate() {
        return expression.firstAggregate();
    }

}
