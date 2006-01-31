package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.Columns;

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
