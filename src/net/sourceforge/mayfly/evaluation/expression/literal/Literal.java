package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.Expression;

public abstract class Literal extends Expression {

    public boolean matchesCell(Cell cell) {
        return cell.equals(Cell.fromContents(valueForCellContentComparison()));
    }

    public final Cell evaluate(Row row) {
        return valueAsCell();
    }

    public final Cell aggregate(Rows rows) {
        return valueAsCell();
    }
    
    abstract public Object valueForCellContentComparison();

    protected abstract Cell valueAsCell();

}
