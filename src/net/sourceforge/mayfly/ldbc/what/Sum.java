package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Sum extends AggregateExpression {

    public Sum(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }

    protected Cell pickOne(Cell min, Cell max, Cell count, Cell sum, Cell average) {
        return sum;
    }

}
