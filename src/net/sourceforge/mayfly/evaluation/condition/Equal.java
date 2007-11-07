package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;

public class Equal extends RowExpression {

    public Equal(Expression leftSide, Expression rightSide) {
        super(leftSide, rightSide);
    }

    @Override
    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.sqlEquals(rightSide);
    }

}
