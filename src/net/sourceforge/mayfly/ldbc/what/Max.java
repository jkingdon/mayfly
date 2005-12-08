package net.sourceforge.mayfly.ldbc.what;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Max extends WhatElement {

    private final SingleColumn column;
    private final String spellingOfMax;

    public Max(SingleColumn column, String spellingOfMax) {
        this.column = column;
        this.spellingOfMax = spellingOfMax;
    }

    public Cell evaluate(Row row) {
        /** Just for checking; aggregation happens in {@link #aggregate(Rows)}. */
        return column.evaluate(row);
    }
    
    public Cell aggregate(Rows rows) {
        if (rows.size() == 0) {
            return NullCell.INSTANCE;
        }
        
        long max = Long.MIN_VALUE;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            long value = column.evaluate(row).asLong();
            if (value > max) {
                max = value;
            }
        }
        return new LongCell(max);
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }
    
    public String firstAggregate() {
        return spellingOfMax + "(" + column.displayName() + ")";
    }

}
