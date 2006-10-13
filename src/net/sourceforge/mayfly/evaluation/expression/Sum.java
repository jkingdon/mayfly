package net.sourceforge.mayfly.evaluation.expression;

import java.util.Collection;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;

public class Sum extends AggregateExpression {

    public Sum(SingleColumn column, String functionName, boolean distinct) {
        this(column, functionName, distinct, Location.UNKNOWN);
    }

    public Sum(SingleColumn column, String functionName, boolean distinct, 
        Location location) {
        super(column, functionName, distinct, location);
    }

    Cell aggregate(Collection values) {
        return aggregateSumAverage(values, true);
    }

    public Expression resolve(ResultRow row) {
        return new Sum((SingleColumn) column.resolve(row), functionName, distinct);
    }

}
