package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;

abstract public class RowExpression extends Condition {

    public final Expression leftSide;
    public final Expression rightSide;

    public RowExpression(Expression leftSide, Expression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(ResultRow row) {
        return compareCellsOrNulls(
            leftSide.evaluate(row), rightSide.evaluate(row));
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

    public String firstAggregate() {
        return Expression.firstAggregate(leftSide, rightSide);
    }
    
    public void check(ResultRow row) {
        leftSide.check(row);
        rightSide.check(row);
    }

}
