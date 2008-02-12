package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

public class CountAll extends Expression {

    private final String functionName;

    public CountAll(String functionName, Location location) {
        super(location);
        this.functionName = functionName;
    }

    public CountAll(String functionName) {
        this(functionName, Location.UNKNOWN);
    }

    @Override
    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        /** This is just for checking; aggregation happens in 
            {@link #aggregate(Rows)}. */
        return new LongCell(0);
    }

    @Override
    public String firstAggregate() {
        return displayName();
    }

    @Override
    public String displayName() {
        return functionName + "(*)";
    }

    @Override
    public Cell aggregate(ResultRows rows) {
        return new LongCell(rows.rowCount());
    }
    
    @Override
    public boolean sameExpression(Expression other) {
        return other instanceof CountAll;
    }

}
