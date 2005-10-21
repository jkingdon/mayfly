package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;

public class AllColumnsFromTable extends WhatElement {
    public static WhatElement fromTree(Tree t) {
        String dimensionIdentifier = t.getFirstChild().getText();
        return new AllColumnsFromTable(dimensionIdentifier);
    }

    private String dimension;

    public AllColumnsFromTable(String dimension) {
        this.dimension = dimension;
    }

    public String columnName() {
        throw new UnimplementedException("selecting everything from a table (select table.*) not implemeneted");
    }

}