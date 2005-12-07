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
