package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

public class IntegerLiteral extends Literal {

    public final int value;

    public IntegerLiteral(int value) {
        this(value, Location.UNKNOWN);
    }

    public IntegerLiteral(int value, Location location) {
        super(location);
        this.value = value;
    }

    public Cell valueAsCell() {
        return new LongCell(value);
    }

    public String displayName() {
        return "" + value;
    }
    
    public boolean sameExpression(Expression other) {
        // Since the choice of IntegerLiteral versus LongLiteral
        // is based just on what range the value is in, saying
        // we are only equal to an expression of our
        // own class works.
        if (other instanceof IntegerLiteral) {
            IntegerLiteral integer = (IntegerLiteral) other;
            return value == integer.value;
        }
        else {
            return false;
        }
    }

}
