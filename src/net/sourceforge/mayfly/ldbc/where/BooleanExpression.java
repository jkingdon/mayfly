package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.util.Selector;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class BooleanExpression extends ValueObject implements Selector {

    public static final BooleanExpression TRUE = new BooleanExpression() {
        public boolean evaluate(ResultRow candidate) {
            return true;
        }

        public String firstAggregate() {
            return null;
        }

    };

    final public boolean evaluate(Object row) {
        if (row instanceof ResultRow) {
            return evaluate((ResultRow)row);
        }
        else if (row instanceof Row) {
            return evaluate(new ResultRow((Row)row));
        }
        else {
            throw new MayflyInternalException("Expected row, got " + row.getClass().getName());
        }
    }
    
    abstract public boolean evaluate(ResultRow row);

    abstract public String firstAggregate();

    public String firstAggregate(BooleanExpression left, BooleanExpression right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

}
