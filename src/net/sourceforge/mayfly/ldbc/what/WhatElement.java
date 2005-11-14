package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;
import org.ldbc.antlr.collections.*;
import org.ldbc.parser.*;

abstract public class WhatElement extends ValueObject {

    abstract public Columns columns();

    public static Object fromExpressionTree(Tree column) {
        switch (column.getType()) {
        case SQLTokenTypes.PARAMETER:
            return JdbcParameter.INSTANCE;

        case SQLTokenTypes.COLUMN:
            AST firstIdentifier = column.getFirstChild();
            AST secondIdentifier = firstIdentifier.getNextSibling();

            if (secondIdentifier == null) {
                String columnName = firstIdentifier.getText();
                return new SingleColumn(columnName);
            } else {
                String tableOrAlias = firstIdentifier.getText();
                String columnName = secondIdentifier.getText();
                return new SingleColumn(tableOrAlias, columnName);
            }

        case SQLTokenTypes.DECIMAL_VALUE:
            return MathematicalInt.fromDecimalValueTree(column);

        default:
            throw new MayflyException("Unrecognized token in what clause at:\n" + column.toString());
        }
    }

    public static Object fromSelectItemTree(Tree t) {
        AST expression = t.getFirstChild();

        Tree column = new Tree(expression.getFirstChild());
        return WhatElement.fromExpressionTree(column);
    }

    //TODO: name sucks
    abstract public Tuples process(Tuples originalTuples, M aliasToTableName);
}
