package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class All extends WhatElement {

    private Columns columns(Row dummyRow) {
        return dummyRow.columns();
    }
    
    public What selected(Row dummyRow) {
        return selectedFromColumns(columns(dummyRow));
    }

    public Cell evaluate(Row row) {
        throw new MayflyInternalException("should have converted this to SingleColumn objects by now");
    }

    public Cell aggregate(Rows rows) {
        throw new MayflyInternalException("should have converted this to SingleColumn objects by now");
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }
}
