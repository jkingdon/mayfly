package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

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
        
        public String firstAggregate() {
            return null;
        }

    };

    abstract public boolean evaluate(Object row);

    abstract public int parameterCount();

    protected int parameterCount(WhatElement expression) {
        return expression instanceof JdbcParameter ? 1 : 0;
    }

    public abstract void substitute(Iterator jdbcParameters);

    protected WhatElement substitute(WhatElement expression, Iterator jdbcParameters) {
        if (expression instanceof JdbcParameter) {
            return Literal.fromValue(jdbcParameters.next());
        } else {
            return expression;
        }
    }

    abstract public String firstAggregate();

    public static String firstAggregate(BooleanExpression left, BooleanExpression right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

}
