package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;

import java.util.Collection;

public class Count extends AggregateExpression {

    public Count(SingleColumn column, String functionName, boolean distinct) {
        super(column, functionName, distinct);
    }

    Cell aggregate(Collection values) {
        return new LongCell(values.size());
    }

}
