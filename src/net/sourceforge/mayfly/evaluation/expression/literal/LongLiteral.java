package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;

public class LongLiteral extends Literal {

    private final long value;

    public LongLiteral(long value) {
        this.value = value;
    }

    protected Cell valueAsCell() {
        return new LongCell(value);
    }

    public Object valueForCellContentComparison() {
        return new Long(value);
    }
    
    public String displayName() {
        return "" + value;
    }
    
    public boolean sameExpression(Expression other) {
        // Since the choice of IntegerLiteral versus LongLiteral
        // is based just on what range the value is in, saying
        // we are only equal to an expression of our
        // own class works.
        if (other instanceof LongLiteral) {
            LongLiteral integer = (LongLiteral) other;
            return value == integer.value;
        }
        else {
            return false;
        }
    }

}
