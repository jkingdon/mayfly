package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.antlr.collections.*;

public class Column extends WhatElement {
    public static Column fromColumnTree(Tree column) {
        AST firstIdentifier = column.getFirstChild();
        AST secondIdentifier = firstIdentifier.getNextSibling();
        
        if (secondIdentifier == null) {
            String columnName = firstIdentifier.getText();
            return new Column(columnName);
        } else {
            String dimensionIdentifier = firstIdentifier.getText();
            String columnName = secondIdentifier.getText();
            return new Column(dimensionIdentifier, columnName);
        }
    }


    private String dimension;
    private String column;

    public Column(String dimension, String column) {
        this.dimension = dimension;
        this.column = column;
    }

    public Column(String column) {
        this(null, column);
    }
    
    public String columnName() {
        return column;
    }

}
