package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;

abstract public class RowExpression extends BooleanExpression {

    public final Expression leftSide;
    public final Expression rightSide;

    public RowExpression(Expression leftSide, Expression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(ResultRow row) {
        return compare(leftSide.evaluate(row), rightSide.evaluate(row));
    }

    abstract protected boolean compare(Cell leftSide, Cell rightSide);

    public String firstAggregate() {
        return Expression.firstAggregate(leftSide, rightSide);
    }

}
