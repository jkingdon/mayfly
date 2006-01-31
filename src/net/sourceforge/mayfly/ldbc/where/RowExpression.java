package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;

abstract public class RowExpression extends BooleanExpression {

    private Expression leftSide;
    private Expression rightSide;

    public RowExpression(Expression leftSide, Expression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;

        return compare(leftSide.evaluate(row), rightSide.evaluate(row));
    }

    abstract protected boolean compare(Cell leftSide, Cell rightSide);

    public String firstAggregate() {
        return Expression.firstAggregate(leftSide, rightSide);
    }

}
