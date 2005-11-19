package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

public abstract class BooleanExpression extends ValueObject implements Selector {

    public static final BooleanExpression TRUE = new BooleanExpression() {
        public boolean evaluate(Object candidate) {
            return true;
        }

        public int parameterCount() {
            return 0;
        }

        public void substitute(Iterator iter) {
        }

    };

    abstract public boolean evaluate(Object row);

    abstract public int parameterCount();

    protected int parameterCount(Transformer expression) {
        return expression instanceof JdbcParameter ? 1 : 0;
    }

    public abstract void substitute(Iterator jdbcParameters);

    protected Transformer substitute(Transformer expression, Iterator jdbcParameters) {
        if (expression instanceof JdbcParameter) {
            return Literal.fromValue(jdbcParameters.next());
        } else {
            return expression;
        }
    }

}
