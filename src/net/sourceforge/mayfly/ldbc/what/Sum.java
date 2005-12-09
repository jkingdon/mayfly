package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Sum extends AggregateExpression {

    public Sum(SingleColumn column, String functionName) {
        super(column, functionName);
    }

    protected long startValue() {
        return 0;
    }

    protected long accumulate(long oldAccumulatedValue, long value) {
        return oldAccumulatedValue + value;
    }

    protected Cell valueForNoRows() {
        // Lame (0 would be more convenient), but standard.
        // I'm not thinking of a way that Mayfly can help with this problem
        // (giving an error and pointing out a better way, or whatever).
        return NullCell.INSTANCE;
    }

}
