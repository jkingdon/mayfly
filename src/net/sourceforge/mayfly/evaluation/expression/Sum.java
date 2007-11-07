package net.sourceforge.mayfly.evaluation.expression;

import java.util.Collection;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;

public class Sum extends AggregateExpression {

    public Sum(Expression column, String functionName, boolean distinct, 
        Location location) {
        super(column, functionName, distinct, location);
    }

    public Sum(Expression column, String functionName, boolean distinct) {
        this(column, functionName, distinct, Location.UNKNOWN);
    }

    @Override
    Cell aggregate(Collection values) {
        return aggregateSumAverage(values, true);
    }

    @Override
    public Expression resolve(ResultRow row) {
        return new Sum(column.resolve(row), functionName, distinct, location);
    }

}
