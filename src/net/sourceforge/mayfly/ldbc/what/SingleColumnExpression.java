package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.antlr.collections.*;

public class SingleColumnExpression extends WhatElement implements Transformer {
    private Column column;

    public static SingleColumnExpression fromExpressionTree(Tree t) {
        AST expression = t.getFirstChild();

        Tree column = new Tree(expression.getFirstChild());
        return fromColumnTree(column);
    }

    public static SingleColumnExpression fromColumnTree(Tree column) {
        AST firstIdentifier = column.getFirstChild();
        AST secondIdentifier = firstIdentifier.getNextSibling();
        
        if (secondIdentifier == null) {
            String columnName = firstIdentifier.getText();
            return new SingleColumnExpression(columnName);
        } else {
            String tableOrAlias = firstIdentifier.getText();
            String columnName = secondIdentifier.getText();
            return new SingleColumnExpression(tableOrAlias, columnName);
        }
    }

    private SingleColumnExpression(Column column) {
        this.column = column;
    }

    public SingleColumnExpression(String columnName) {
        this(null, columnName);
    }

    public SingleColumnExpression(String tableOrAlias, String columnName) {
        this(new Column(tableOrAlias, columnName));
    }

    public Columns columns() {
        return new Columns(new ImmutableList(column));
    }

    public Object transform(Object from) {
        Row row = (Row) from;
        return row.cell(column);
    }

}
