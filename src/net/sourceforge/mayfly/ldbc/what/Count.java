package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Count extends AggregateExpression {

    public Count(SingleColumn column, String functionName) {
        super(column, functionName);
    }

    protected long startValue() {
        return 0;
    }

    protected long accumulate(long oldAccumulatedValue, long value) {
        return oldAccumulatedValue + 1;
    }

    protected Cell valueForNoRows() {
        return new LongCell(0);
    }

}
