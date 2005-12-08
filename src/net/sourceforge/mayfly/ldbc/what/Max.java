package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class Max extends WhatElement {

    private final SingleColumn column;
    private final String spellingOfMax;

    public Max(SingleColumn column, String spellingOfMax) {
        this.column = column;
        this.spellingOfMax = spellingOfMax;
    }

    public Cell evaluate(Row row) {
        throw new UnimplementedException();
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }
    
    public String firstAggregate() {
        return spellingOfMax + "(" + column.displayName() + ")";
    }

}
