package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.Row;

public class All extends WhatElement {

    private Columns columns(Row dummyRow) {
        return dummyRow.columns();
    }
    
    public Selected selected(Row dummyRow) {
        return selectedFromColumns(columns(dummyRow));
    }

    public String displayName() {
        return "*";
    }

}
