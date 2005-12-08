package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class Max extends WhatElement {

    public Max(SingleColumn column) {
    }

    public Cell evaluate(Row row) {
        throw new UnimplementedException();
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }
    
    public boolean isAggregate() {
        return true;
    }

}
