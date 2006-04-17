package net.sourceforge.mayfly.evaluation.expression;

import java.util.Collection;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;

public class Sum extends AggregateExpression {

    public Sum(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }

    Cell aggregate(Collection values) {
        return aggregateSumAverage(values, true);
    }

}
