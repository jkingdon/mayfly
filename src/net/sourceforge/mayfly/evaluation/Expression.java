package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.parser.Location;

import java.util.Collections;

abstract public class Expression extends WhatElement {
    
    public final Location location;
    
    protected Expression(Location location) {
        this.location = location;
    }
    
    protected Expression() {
        this(Location.UNKNOWN);
    }

    public Selected selected(ResultRow dummyRow) {
        return new Selected(Collections.singletonList(resolve(dummyRow)));
    }

    public String firstColumn() {
        return null;
    }
    
    public String firstAggregate() {
        return null;
    }

    abstract public Cell evaluate(ResultRow row);

    public Cell evaluate(Row row, String table) {
        return evaluate(new ResultRow(row, table));
    }
    
    public void check(ResultRow row) {
    }

    abstract public Cell aggregate(ResultRows rows);

    abstract public boolean sameExpression(Expression other);

    public Expression resolve(ResultRow row) {
        return this;
    }
    
    public static String firstAggregate(Expression left, Expression right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

    public final boolean matches(Expression expression) {
        return expression.sameExpression(this);
    }

    public boolean matches(String columnName) {
        return false;
    }

}
