package net.sourceforge.mayfly.evaluation.expression;

import java.util.Collection;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;

public class Minimum extends AggregateExpression {

    public Minimum(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }
    
    Cell aggregate(Collection values) {
        return aggregateMinMax(values);
    }
    
    boolean isBetter(Cell candidate, Cell bestSoFar) {
        return candidate.compareTo(bestSoFar) < 0;
    }

}
