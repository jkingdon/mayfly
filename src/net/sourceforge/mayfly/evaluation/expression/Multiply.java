package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;

public class Multiply extends BinaryOperator {

    public Multiply(Expression left, Expression right) {
        super(left, right);
    }

    protected Cell combine(Cell left, Cell right) {
        return new LongCell(left.asLong() * right.asLong());
    }

    public Expression resolveAndReturn(Row row) {
        return new Multiply(left.resolveAndReturn(row), right.resolveAndReturn(row));
    }

}
