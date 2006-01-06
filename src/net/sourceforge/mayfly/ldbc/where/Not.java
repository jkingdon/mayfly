package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

public class Not extends BooleanExpression {

    private final BooleanExpression operand;

    public Not(BooleanExpression operand) {
        this.operand = operand;
    }

    public boolean evaluate(Object row) {
        return !operand.evaluate(row);
    }

    public int parameterCount() {
        return operand.parameterCount();
    }

    public void substitute(Iterator jdbcParameters) {
        operand.substitute(jdbcParameters);
    }

}
