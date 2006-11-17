package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;

public class LessEqual extends RowExpression {

    public LessEqual(Expression leftSide, Expression rightSide) {
        super(leftSide, rightSide);
    }

    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.compareTo(rightSide) <= 0;
    }

}
