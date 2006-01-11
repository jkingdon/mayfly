package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class Minimum extends AggregateExpression {

    public Minimum(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }
    
    protected Cell pickOne(Cell minimum, Cell maximum, Cell count, Cell sum, Cell average) {
        return minimum;
    }

}
