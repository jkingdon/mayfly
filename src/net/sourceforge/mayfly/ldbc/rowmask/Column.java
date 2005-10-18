package net.sourceforge.mayfly.ldbc.rowmask;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.antlr.collections.*;

public class Column extends RowMaskElement {
    public static net.sourceforge.mayfly.ldbc.rowmask.Column fromColumnTree(Tree column) {
        AST dimensionIdentifier = column.getFirstChild();
        AST columnName = dimensionIdentifier.getNextSibling();

        return new net.sourceforge.mayfly.ldbc.rowmask.Column(dimensionIdentifier.getText(), columnName.getText());
    }


    private String dimension;
    private String column;

    public Column(String dimension, String column) {
        this.dimension = dimension;
        this.column = column;
    }


}
