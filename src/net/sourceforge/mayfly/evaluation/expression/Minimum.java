package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;

import java.util.Collection;

public class Minimum extends AggregateExpression {

    public Minimum(SingleColumn column, String functionName, boolean distinct, Location location) {
        super(column, functionName, distinct, location);
    }
    
    public Minimum(SingleColumn column, String functionName, boolean distinct) {
        this(column, functionName, distinct, Location.UNKNOWN);
    }
    
    Cell aggregate(Collection values) {
        return aggregateMinMax(values);
    }
    
    boolean isBetter(Cell candidate, Cell bestSoFar) {
        return candidate.compareTo(bestSoFar, location) < 0;
    }

    public Expression resolve(ResultRow row) {
        return new Minimum((SingleColumn) column.resolve(row), functionName, distinct, location);
    }

}
