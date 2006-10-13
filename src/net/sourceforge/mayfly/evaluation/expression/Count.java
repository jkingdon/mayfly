package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;

import java.util.Collection;

public class Count extends AggregateExpression {

    public Count(SingleColumn column, String functionName, boolean distinct,
        Location location) {
        super(column, functionName, distinct, location);
    }

    public Count(SingleColumn column, String functionName, boolean distinct) {
        this(column, functionName, distinct, Location.UNKNOWN);
    }

    Cell aggregate(Collection values) {
        return new LongCell(values.size());
    }

    public Expression resolve(ResultRow row) {
        return new Count((SingleColumn) column.resolve(row), functionName, distinct, location);
    }

}
