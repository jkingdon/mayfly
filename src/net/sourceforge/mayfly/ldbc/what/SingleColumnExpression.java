package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.antlr.collections.*;

public class SingleColumnExpression extends WhatElement {
    private Column column;

    public static SingleColumnExpression fromExpressionTree(Tree t) {
        AST expression = t.getFirstChild();

        Tree column = new Tree(expression.getFirstChild());
        return new SingleColumnExpression(Column.fromColumnTree(column));
    }

    public SingleColumnExpression(Column column) {
        this.column = column;
    }

    public String columnName() {
        return column.columnName();
    }
}
