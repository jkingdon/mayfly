package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
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

    public Cell evaluate(ResultRow row) {
        /** This is just for checking; aggregation happens in 
            {@link #aggregate(Rows)}. */
        return new LongCell(0);
    }

    public String firstAggregate() {
        return displayName();
    }

    public String displayName() {
        return functionName + "(*)";
    }

    public Cell aggregate(ResultRows rows) {
        return new LongCell(rows.size());
    }
    
    public boolean sameExpression(Expression other) {
        return other instanceof CountAll;
    }

}
