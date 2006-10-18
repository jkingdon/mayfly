package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.util.Selector;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class Condition extends ValueObject implements Selector {

    public static final Condition TRUE = new True();

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

    public String firstAggregate(Condition left, Condition right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

    abstract public void check(ResultRow row);

}
