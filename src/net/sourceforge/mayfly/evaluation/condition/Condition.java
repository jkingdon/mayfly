package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class Condition extends ValueObject {

    public static final Condition TRUE = new True();

    final public boolean evaluate(Row row, String table) {
        return evaluate(new ResultRow(row, table));
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
