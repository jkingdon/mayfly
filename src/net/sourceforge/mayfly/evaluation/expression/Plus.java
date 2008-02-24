package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class Plus extends BinaryOperator {

    public Plus(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected Cell combine(Cell left, Cell right) {
        return new LongCell(left.asLong() + right.asLong());
    }

    @Override
    public Expression resolve(ResultRow row, Evaluator evaluator) {
        return new Plus(left.resolve(row, evaluator), right.resolve(row, evaluator));
    }

}
