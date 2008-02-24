package net.sourceforge.mayfly.evaluation.expression;

import java.util.Collection;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

public class Maximum extends AggregateExpression {

    public Maximum(Expression column, String spellingOfMax, boolean distinct, Location location) {
        super(column, spellingOfMax, distinct, location);
    }
    
    public Maximum(Expression column, String spellingOfMax, boolean distinct) {
        this(column, spellingOfMax, distinct, Location.UNKNOWN);
    }
    
    @Override
    Cell aggregate(Collection values) {
        return aggregateMinMax(values);
    }
    
    @Override
    boolean isBetter(Cell candidate, Cell bestSoFar) {
        return candidate.compareTo(bestSoFar, location) > 0;
    }

    @Override
    public Expression resolve(ResultRow row, Evaluator evaluator) {
        return new Maximum(column.resolve(row, evaluator), functionName, distinct, location);
    }

}
