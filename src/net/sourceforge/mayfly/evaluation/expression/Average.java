package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;

import java.util.Collection;

public class Average extends AggregateExpression {

    public Average(SingleColumn column, String functionName, boolean distinct,
        Location location) {
        super(column, functionName, distinct, location);
    }

    public Average(SingleColumn column, String functionName, boolean distinct) {
        this(column, functionName, distinct, Location.UNKNOWN);
    }

    Cell aggregate(Collection values) {
        return aggregateSumAverage(values, false);
    }
    
    public Expression resolve(ResultRow row) {
        return new Average((SingleColumn) column.resolve(row), 
            functionName, distinct);
    }

}
