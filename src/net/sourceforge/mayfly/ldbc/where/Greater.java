package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;

public class Greater extends RowExpression {

    public Greater(Expression leftSide, Expression rightSide) {
        super(leftSide, rightSide);
    }

    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.asLong() > rightSide.asLong();
    }

}
