package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;

public class Concatenate extends BinaryOperator {

    public Concatenate(Expression left, Expression right) {
        super(left, right);
    }

    protected Cell combine(Cell leftCell, Cell rightCell) {
        return new StringCell(leftCell.asString() + rightCell.asString());
    }
    
    public Expression resolve(ResultRow row) {
        return new Concatenate(
            left.resolve(row), right.resolve(row));
    }

}
