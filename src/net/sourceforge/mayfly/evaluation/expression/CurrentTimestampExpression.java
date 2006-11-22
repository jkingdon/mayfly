package net.sourceforge.mayfly.evaluation.expression;

import org.joda.time.LocalDateTime;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.TimestampCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class CurrentTimestampExpression extends Expression {

    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        return valueAsCell();
    }

    public Cell aggregate(ResultRows rows) {
        return valueAsCell();
    }

    private Cell valueAsCell() {
        /* Someday we might need to worry about getting the "current" time
           from a transaction log or synchronized across replicated databases
           or something.  That day is not yet here.
           
           Do note that the timezone here is the one of the current machine.
        */
        return new TimestampCell(new LocalDateTime(System.currentTimeMillis()));
    }

    public boolean sameExpression(Expression other) {
        return other instanceof CurrentTimestampExpression;
    }

    public String displayName() {
        return "current_timestamp";
    }

}
