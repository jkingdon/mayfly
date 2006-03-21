package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.what.Selected;

public class AllColumnsFromTable extends WhatElement {

    private String aliasOrTable;

    public AllColumnsFromTable(String aliasOrTable) {
        this.aliasOrTable = aliasOrTable;
    }

    private Columns columns(Row dummyRow) {
        return dummyRow.columnsForTable(aliasOrTable);
    }
    
    public Selected selected(Row dummyRow) {
        return selectedFromColumns(columns(dummyRow));
    }

    public String displayName() {
        return aliasOrTable + ".*";
    }

}
