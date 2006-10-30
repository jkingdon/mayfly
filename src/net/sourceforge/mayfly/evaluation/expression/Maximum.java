package net.sourceforge.mayfly.evaluation.expression;

import java.util.Collection;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;

public class Maximum extends AggregateExpression {

    public Maximum(SingleColumn column, String spellingOfMax, boolean distinct, Location location) {
        super(column, spellingOfMax, distinct, location);
    }
    
    public Maximum(SingleColumn column, String spellingOfMax, boolean distinct) {
        this(column, spellingOfMax, distinct, Location.UNKNOWN);
    }
    
    Cell aggregate(Collection values) {
        return aggregateMinMax(values);
    }
    
    boolean isBetter(Cell candidate, Cell bestSoFar) {
        return candidate.compareTo(bestSoFar, location) > 0;
    }

    public Expression resolve(ResultRow row) {
        return new Maximum((SingleColumn) column.resolve(row), functionName, distinct);
    }

}
