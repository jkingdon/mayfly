package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Max extends AggregateExpression {

    public Max(SingleColumn column, String spellingOfMax) {
        super(column, spellingOfMax);
    }

    protected long startValue() {
        return Long.MIN_VALUE;
    }

    protected long accumulate(long oldAccumulatedValue, long value) {
        return Math.max(oldAccumulatedValue, value);
    }

    protected Cell valueForNoRows() {
        return NullCell.INSTANCE;
    }

}
