package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class Greater extends RowExpression {

    public Greater(Expression leftSide, Expression rightSide) {
        super(leftSide, rightSide);
    }

    @Override
    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.compareTo(rightSide) > 0;
    }

    @Override
    public Condition resolve(ResultRow row, Evaluator evaluator) {
        Expression newLeftSide = leftSide.resolve(row, evaluator);
        Expression newRightSide = rightSide.resolve(row, evaluator);
        if (newLeftSide != leftSide || newRightSide != rightSide) {
            return new Greater(newLeftSide, newRightSide);
        }
        else {
            return this;
        }
    }

}
