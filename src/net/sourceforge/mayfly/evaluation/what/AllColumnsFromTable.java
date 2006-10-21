package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.evaluation.ResultRow;

public class AllColumnsFromTable extends WhatElement {

    private String aliasOrTable;

    public AllColumnsFromTable(String aliasOrTable) {
        this.aliasOrTable = aliasOrTable;
    }

    public Selected selected(ResultRow dummyRow) {
        return new Selected(dummyRow.expressionsForTable(aliasOrTable));
    }

    public String displayName() {
        return aliasOrTable + ".*";
    }

}
