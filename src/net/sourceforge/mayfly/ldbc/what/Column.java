package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.antlr.collections.*;

public class Column extends WhatElement {
    public static Column fromColumnTree(Tree column) {
        AST dimensionIdentifier = column.getFirstChild();
        AST columnName = dimensionIdentifier.getNextSibling();

        return new Column(dimensionIdentifier.getText(), columnName.getText());
    }


    private String dimension;
    private String column;

    public Column(String dimension, String column) {
        this.dimension = dimension;
        this.column = column;
    }


}
