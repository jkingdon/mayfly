package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

import java.util.Collection;

public class Average extends AggregateExpression {

    public Average(Expression column, String functionName, boolean distinct,
        Location location) {
        super(column, functionName, distinct, location);
    }

    public Average(Expression column, String functionName, boolean distinct) {
        this(column, functionName, distinct, Location.UNKNOWN);
    }

    @Override
    Cell aggregate(Collection values) {
        return aggregateSumAverage(values, false);
    }
    
    @Override
    public Expression resolve(ResultRow row, Evaluator evaluator) {
        return new Average(column.resolve(row, evaluator), 
            functionName, distinct, location);
    }

}
