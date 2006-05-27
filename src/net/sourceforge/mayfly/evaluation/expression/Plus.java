package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;

public class Plus extends BinaryOperator {

    public Plus(Expression left, Expression right) {
        super(left, right);
    }

    protected Cell combine(Cell left, Cell right) {
        return new LongCell(left.asLong() + right.asLong());
    }

    public Expression resolveAndReturn(Row row) {
        return new Plus(left.resolveAndReturn(row), right.resolveAndReturn(row));
    }

}
