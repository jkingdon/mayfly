package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.util.Selector;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class BooleanExpression extends ValueObject implements Selector {

    public static final BooleanExpression TRUE = new BooleanExpression() {
        public boolean evaluate(Object candidate) {
            return true;
        }

        public String firstAggregate() {
            return null;
        }

    };

    abstract public boolean evaluate(Object row);

    abstract public String firstAggregate();

    public String firstAggregate(BooleanExpression left, BooleanExpression right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

}
