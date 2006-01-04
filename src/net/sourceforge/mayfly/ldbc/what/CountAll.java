package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class CountAll extends WhatElement implements Transformer {

    private final String functionName;

    public CountAll(String functionName) {
        this.functionName = functionName;
    }

    public Cell evaluate(Row row) {
        /** This is just for checking; aggregation happens in {@link #aggregate(Rows)}. */
        return new LongCell(0);
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }

    public String firstAggregate() {
        return functionName + "(*)";
    }

    public Cell aggregate(Rows rows) {
        return new LongCell(rows.size());
    }

    public Object transform(Object from) {
        throw new UnimplementedException();
    }

}
