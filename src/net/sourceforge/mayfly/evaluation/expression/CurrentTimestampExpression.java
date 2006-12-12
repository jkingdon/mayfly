package net.sourceforge.mayfly.evaluation.expression;

import org.joda.time.LocalDateTime;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.TimestampCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

public class CurrentTimestampExpression extends Expression {
    
    private TimeSource timeSource;

    public CurrentTimestampExpression(Location location, TimeSource timeSource) {
        super(location);
        this.timeSource = timeSource;
    }

    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        return valueAsCell();
    }

    public Cell aggregate(ResultRows rows) {
        return valueAsCell();
    }

    private Cell valueAsCell() {
        /* Note that the timezone here is the one of the current machine. */
        return new TimestampCell(new LocalDateTime(timeSource.current()));
    }

    public boolean sameExpression(Expression other) {
        return other instanceof CurrentTimestampExpression;
    }

    public String displayName() {
        return "current_timestamp";
    }
    
    public String asSql() {
        return "CURRENT_TIMESTAMP";
    }

}
