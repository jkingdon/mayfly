package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Expression;

public class Concatenate extends BinaryOperator {

    public Concatenate(Expression left, Expression right) {
        super(left, right);
    }

    protected Cell combine(Cell leftCell, Cell rightCell) {
        return new StringCell(leftCell.asString() + rightCell.asString());
    }

}
