package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

public abstract class Literal extends Expression {

    protected Literal(Location location) {
        super(location);
    }

    @Override
    public final Cell evaluate(ResultRow row, Evaluator evaluator) {
        return valueAsCell();
    }

    @Override
    public final Cell aggregate(ResultRows rows) {
        return valueAsCell();
    }
    
    public abstract Cell valueAsCell();

}
