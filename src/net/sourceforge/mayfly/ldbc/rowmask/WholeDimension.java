package net.sourceforge.mayfly.ldbc.rowmask;

import net.sourceforge.mayfly.ldbc.*;

public class WholeDimension extends RowMaskElement {
    public static RowMaskElement fromTree(Tree t) {
        String dimensionIdentifier = t.getFirstChild().getText();
        return new net.sourceforge.mayfly.ldbc.rowmask.WholeDimension(dimensionIdentifier);
    }

    private String dimension;

    public WholeDimension(String dimension) {
        this.dimension = dimension;
    }
}
