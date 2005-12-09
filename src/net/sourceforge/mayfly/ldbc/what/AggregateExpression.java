package net.sourceforge.mayfly.ldbc.what;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public abstract class AggregateExpression extends WhatElement {

    private final SingleColumn column;
    private final String functionName;

    protected AggregateExpression(SingleColumn column, String functionName) {
        this.column = column;
        this.functionName = functionName;
    }

    public Cell evaluate(Row row) {
        /** This is just for checking; aggregation happens in {@link #aggregate(Rows)}. */
        return column.evaluate(row);
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }

    public String firstAggregate() {
        return functionName + "(" + column.displayName() + ")";
    }

    protected abstract long startValue();

    protected abstract long accumulate(long oldAccumulatedValue, long value);

    protected abstract Cell valueForNoRows();

    public Cell aggregate(Rows rows) {
        boolean foundOne = false;
        long accumulatedValue = startValue();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            Cell cell = evaluate(row);
            if (!(cell instanceof NullCell)) {
                long value = cell.asLong();
                foundOne = true;
                accumulatedValue = accumulate(accumulatedValue, value);
            }
        }
        
        if (foundOne) {
            return new LongCell(accumulatedValue);
        } else {
            return valueForNoRows();
        }
    }

}
