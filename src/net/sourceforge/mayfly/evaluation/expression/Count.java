package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;

import java.util.Collection;

public class Count extends AggregateExpression {

    public Count(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }

    Cell aggregate(Collection values) {
        return new LongCell(values.size());
    }

    public Expression resolveAndReturn(Row row) {
        return new Count((SingleColumn) column.resolveAndReturn(row), functionName, distinct);
    }

}
