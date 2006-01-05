package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Max extends AggregateExpression {

    public Max(SingleColumn column, String spellingOfMax, boolean distinct) {
        super(column, spellingOfMax, distinct);
    }

    protected Cell pickOne(Cell min, Cell max, Cell count, Cell sum, Cell average) {
        return max;
    }

}
