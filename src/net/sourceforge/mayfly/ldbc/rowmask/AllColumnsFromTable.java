package net.sourceforge.mayfly.ldbc.rowmask;

import net.sourceforge.mayfly.ldbc.*;

public class AllColumnsFromTable extends RowMaskElement {
    public static RowMaskElement fromTree(Tree t) {
        String dimensionIdentifier = t.getFirstChild().getText();
        return new net.sourceforge.mayfly.ldbc.rowmask.AllColumnsFromTable(dimensionIdentifier);
    }

    private String dimension;

    public AllColumnsFromTable(String dimension) {
        this.dimension = dimension;
    }
}
