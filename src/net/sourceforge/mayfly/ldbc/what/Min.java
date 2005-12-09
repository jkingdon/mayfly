package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Min extends AggregateExpression {

    public Min(SingleColumn column, String functionName) {
        super(column, functionName);
    }

    protected Cell valueForNoRows() {
        return NullCell.INSTANCE;
    }

    protected long accumulate(long oldAccumulatedValue, long value) {
        return Math.min(value, oldAccumulatedValue);
    }

    protected long startValue() {
        return Long.MAX_VALUE;
    }

}
