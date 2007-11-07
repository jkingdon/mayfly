package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

abstract public class RowExpression extends Condition {

    public final Expression leftSide;
    public final Expression rightSide;

    public RowExpression(Expression leftSide, Expression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    @Override
    public boolean evaluate(ResultRow row, Evaluator evaluator) {
        return compareCellsOrNulls(
            leftSide.evaluate(row, evaluator), 
            rightSide.evaluate(row, evaluator));
    }

    private boolean compareCellsOrNulls(Cell leftCell, Cell rightCell) {
        if (leftCell instanceof NullCell) {
            return false;
        }
        if (rightCell instanceof NullCell) {
            return false;
        }
        return compare(leftCell, rightCell);
    }

    abstract protected boolean compare(Cell leftSide, Cell rightSide);

    @Override
    public String firstAggregate() {
        return Expression.firstAggregate(leftSide, rightSide);
    }
    
    @Override
    public void check(ResultRow row) {
        leftSide.check(row);
        rightSide.check(row);
    }

}
