package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public abstract class BooleanExpression extends ValueObject implements Selector {

    public static final BooleanExpression TRUE = new BooleanExpression() {
        public boolean evaluate(Object candidate) {
            return true;
        }

        public int parameterCount() {
            return 0;
        }
    };

    abstract public boolean evaluate(Object candidate);

    abstract public int parameterCount();

    protected int parameterCount(Transformer expression) {
        return expression instanceof JdbcParameter ? 1 : 0;
    }

}
