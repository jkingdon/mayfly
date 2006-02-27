package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.ldbc.Rows;
import net.sourceforge.mayfly.ldbc.what.WhatElement;

abstract public class Expression extends WhatElement {

    public String firstColumn() {
        return null;
    }
    
    public String firstAggregate() {
        return null;
    }

    abstract public Cell evaluate(Row row);

    abstract public Cell aggregate(Rows rows);

    abstract public boolean sameExpression(Expression other);

    public static String firstAggregate(Expression left, Expression right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

    public Cell findValue(int zeroBasedColumn, Row row) {
        return evaluate(row);
    }

    public boolean matches(Column column) {
        return false;
    }

}
