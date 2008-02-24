package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class Concatenate extends BinaryOperator {

    public Concatenate(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected Cell combine(Cell leftCell, Cell rightCell) {
        return new StringCell(leftCell.asString() + rightCell.asString());
    }
    
    @Override
    public Expression resolve(ResultRow row, Evaluator evaluator) {
        return new Concatenate(
            left.resolve(row, evaluator), right.resolve(row, evaluator));
    }

}
