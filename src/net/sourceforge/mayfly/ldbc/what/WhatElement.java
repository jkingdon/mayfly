package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.Iterator;

/**
 * @internal
 * Something mention in a select clause (*, table.*, or expression).
 * It is possible that the expression case should be handled by having
 * a subclass which wraps
 * an expression, rather than having {@link net.sourceforge.mayfly.evaluation.Expression}
 * inherit from us.  Then the distinction between {@link net.sourceforge.mayfly.evaluation.what.Selected}
 * and {@link net.sourceforge.mayfly.ldbc.what.What} would carry down
 * to here.
 */
abstract public class WhatElement extends ValueObject {

    abstract public Selected selected(Row dummyRow);

    abstract public String displayName();

    protected Selected selectedFromColumns(Columns columns) {
        Selected result = new Selected();
        Iterator iter = columns.iterator();
        while (iter.hasNext()) {
            Column column = (Column) iter.next();
            result.add(new SingleColumn(column.tableOrAlias(), column.columnName()));
        }
        return result;
    }

}
