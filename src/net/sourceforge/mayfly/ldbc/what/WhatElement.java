package net.sourceforge.mayfly.ldbc.what;

import org.ldbc.antlr.collections.*;
import org.ldbc.parser.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

abstract public class WhatElement extends ValueObject {

    abstract public Columns columns();

    public static Object fromColumnTree(Tree column) {
        switch (column.getType()) {
        case SQLTokenTypes.PARAMETER:
            return JdbcParameter.INSTANCE;
    
        case SQLTokenTypes.COLUMN:
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
    
        default:
            throw new MayflyException("Unrecognized syntax at:\n" + column.toString());
        }
    }

    public static Object fromExpressionTree(Tree t) {
        AST expression = t.getFirstChild();
    
        Tree column = new Tree(expression.getFirstChild());
        return WhatElement.fromColumnTree(column);
    }

}
