package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

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
