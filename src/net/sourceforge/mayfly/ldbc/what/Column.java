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
    private String columnName;

    public Column(String dimension, String columnName) {
        this.dimension = dimension;
        this.columnName = columnName;
    }

    public Column(String column) {
        this(null, column);
    }
    
    public String columnName() {
        return columnName;
    }

    public boolean equals(Object other) {
        return columnName.equalsIgnoreCase(((Column)other).columnName);
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return columnName;
    }

}
