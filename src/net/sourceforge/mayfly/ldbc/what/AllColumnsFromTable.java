package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class AllColumnsFromTable extends WhatElement {
    public static WhatElement fromTree(Tree t) {
        String dimensionIdentifier = t.getFirstChild().getText();
        return new AllColumnsFromTable(dimensionIdentifier);
    }

    private String aliasOrTable;

    public AllColumnsFromTable(String aliasOrTable) {
        this.aliasOrTable = aliasOrTable;
    }

    public Columns columns(Row dummyRow) {
        return dummyRow.columnsForTable(aliasOrTable);
    }

    public Tuple process(Tuple originalTuple, M aliasToTableName) {
        throw new UnimplementedException();
    }
}
