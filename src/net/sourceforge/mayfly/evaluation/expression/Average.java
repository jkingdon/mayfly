package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;

import java.util.Collection;

public class Average extends AggregateExpression {

    public Average(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }

    Cell aggregate(Collection values) {
        return aggregateSumAverage(values, false);
    }
    
    public Expression resolveAndReturn(Row row) {
        return new Average((SingleColumn) column.resolveAndReturn(row), functionName, distinct);
    }

}
