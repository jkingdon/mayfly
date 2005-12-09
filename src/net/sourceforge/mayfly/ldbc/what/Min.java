package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Min extends AggregateExpression {

    public Min(SingleColumn column, String functionName) {
        super(column, functionName);
    }
    
    protected Cell pickOne(Cell min, Cell max, Cell count, Cell sum, Cell average) {
        return min;
    }

}
