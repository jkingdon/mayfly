package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

public class Where extends BooleanExpression {

    public static final Where EMPTY = new Where(BooleanExpression.TRUE);

    private BooleanExpression expression = BooleanExpression.TRUE;

    public Where(BooleanExpression expression) {
        this.expression = expression;
    }

    public boolean evaluate(Object candidate) {
        return expression.evaluate(candidate);
    }

    public int parameterCount() {
        return expression.parameterCount();
    }

    public void substitute(Iterator jdbcParameters) {
        expression.substitute(jdbcParameters);
    }


}
