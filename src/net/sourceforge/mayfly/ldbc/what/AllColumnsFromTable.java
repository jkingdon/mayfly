package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class AllColumnsFromTable extends WhatElement {
    public static WhatElement fromTree(Tree t) {
        String dimensionIdentifier = t.getFirstChild().getText();
        return new AllColumnsFromTable(dimensionIdentifier);
    }

    private String aliasOrTable;

    public AllColumnsFromTable(String aliasOrTable) {
        this.aliasOrTable = aliasOrTable;
    }

    private Columns columns(Row dummyRow) {
        return dummyRow.columnsForTable(aliasOrTable);
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
