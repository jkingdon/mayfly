package net.sourceforge.mayfly.ldbc.what;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;
import antlr.collections.*;

/**
 * @internal
 * Plays the role of an expression (well, when we're not using
 * {@link net.sourceforge.mayfly.util.Transformer} for that).
 * But also indicates something mentioned in a select clause.
 * 
 * The difference is that SELECT * FROM foo has one
 * WhatElement in the sense of "something mentioned in a
 * select clause", but has several in the sense of "expression".
 * The former is converted to the latter by {@link net.sourceforge.mayfly.ldbc.what.What#selected(Row)}.
 * 
 * It is possible we should separate these two roles; I suspect
 * it would clean up the aggregation code, for example.
 */
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
            
        case SQLTokenTypes.LITERAL_max: {
            Tree column = (Tree) expression.children().element(0);
            return new Max((SingleColumn) fromExpressionTree(column), expression.getText());
        }

        case SQLTokenTypes.LITERAL_min: {
            Tree column = (Tree) expression.children().element(0);
            return new Min((SingleColumn) fromExpressionTree(column), expression.getText());
        }

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

    abstract public Cell aggregate(Rows rows);

    //TODO: name sucks
    abstract public Tuple process(Tuple originalTuple, M aliasToTableName);
    
    public String firstAggregate() {
        return null;
    }

    public String firstColumn() {
        return null;
    }

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
