package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.Expression;

public abstract class Literal extends Expression {

    public final Cell evaluate(Row row) {
        return valueAsCell();
    }

    public final Cell aggregate(Rows rows) {
        return valueAsCell();
    }
    
    protected abstract Cell valueAsCell();

}
