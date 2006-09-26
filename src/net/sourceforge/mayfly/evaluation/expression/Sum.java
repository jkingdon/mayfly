package net.sourceforge.mayfly.evaluation.expression;

import java.util.Collection;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
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

    public Expression resolveAndReturn(Row row) {
        return new Sum((SingleColumn) column.resolveAndReturn(row), functionName, distinct);
    }

}
