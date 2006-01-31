package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

/**
 * @internal
 * Plays the role of an expression (when it is a
 * {@link net.sourceforge.mayfly.evaluation.Expression}).
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

    public Selected selected(Row dummyRow) {
        return new Selected(Collections.singletonList(this));
    }

    public String firstAggregate() {
        return null;
    }

    public String firstColumn() {
        return null;
    }
    
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

    public boolean matches(Column column) {
        return false;
    }

}
