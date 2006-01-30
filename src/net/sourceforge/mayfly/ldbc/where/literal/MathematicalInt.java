package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.Expression;

public class MathematicalInt extends Literal {

    private final int value;

    public MathematicalInt(int value) {
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
        if (other instanceof MathematicalInt) {
            MathematicalInt integer = (MathematicalInt) other;
            return value == integer.value;
        }
        else {
            return false;
        }
    }

}
