package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;

public class Multiply extends BinaryOperator {

    public Multiply(Expression left, Expression right) {
        super(left, right);
    }

    protected Cell combine(Cell left, Cell right) {
        return new LongCell(left.asLong() * right.asLong());
    }

    public Expression resolve(ResultRow row) {
        return new Multiply(left.resolve(row), right.resolve(row));
    }

}
