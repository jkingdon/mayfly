package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

abstract public class Expression extends WhatElement {

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

}
