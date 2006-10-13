package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
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

    public Selected selected(Row dummyRow) {
        return new Selected(Collections.singletonList(resolveAndReturn(dummyRow)));
    }

    public String firstColumn() {
        return null;
    }
    
    public String firstAggregate() {
        return null;
    }

    abstract public Cell evaluate(ResultRow row);

    public Cell evaluate(Row row) {
        return evaluate(new ResultRow(row));
    }

    abstract public Cell aggregate(ResultRows rows);

    public Cell aggregate(Rows rows) {
        return aggregate(new ResultRows(rows));
    }

    abstract public boolean sameExpression(Expression other);

    public Expression resolve(ResultRow row) {
        return this;
    }
    
    public final Expression resolveAndReturn(Row row) {
        return resolve(new ResultRow(row));
    }

    public static String firstAggregate(Expression left, Expression right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

    public boolean matches(Column column) {
        return false;
    }

    public final boolean matches(Expression expression) {
        return expression.sameExpression(this);
    }

    public boolean matches(String columnName) {
        return false;
    }

}
