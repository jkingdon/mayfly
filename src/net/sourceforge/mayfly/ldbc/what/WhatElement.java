package net.sourceforge.mayfly.ldbc.what;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;
import antlr.collections.*;

abstract public class WhatElement extends ValueObject {

    public static WhatElement fromExpressionTree(Tree expression) {
        switch (expression.getType()) {
        case SQLTokenTypes.PARAMETER:
            return JdbcParameter.INSTANCE;

        case SQLTokenTypes.COLUMN:
            AST firstIdentifier = expression.getFirstChild();
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
            return MathematicalInt.fromDecimalValueTree(expression);

        case SQLTokenTypes.QUOTED_STRING:
            return QuotedString.fromQuotedStringTree(expression);

        case SQLTokenTypes.VERTBARS:
            L children = expression.children().asList();
            Tree left = (Tree) children.get(0);
            Tree right = (Tree) children.get(1);
            return new Concatenate(fromExpressionTree(left), fromExpressionTree(right));

        default:
            throw new MayflyException("Unrecognized token in what clause at:\n" + expression.toString());
        }
    }

    public static Object fromSelectItemTree(Tree t) {
        AST expression = t.getFirstChild();

        Tree column = new Tree(expression.getFirstChild());
        return WhatElement.fromExpressionTree(column);
    }

    public What selected(Row dummyRow) {
        return new What(Collections.singletonList(this));
    }

    abstract public Cell evaluate(Row row);

    //TODO: name sucks
    abstract public Tuple process(Tuple originalTuple, M aliasToTableName);

    protected What selectedFromColumns(Columns columns) {
        L result = new L();
        Iterator iter = columns.iterator();
        while (iter.hasNext()) {
            Column column = (Column) iter.next();
            result.add(new SingleColumn(column.tableOrAlias(), column.columnName()));
        }
        return new What(result);
    }

}
