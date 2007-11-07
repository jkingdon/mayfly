package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

public class LongLiteral extends Literal {

    public final long value;

    public LongLiteral(long value) {
        this(value, Location.UNKNOWN);
    }

    public LongLiteral(long value, Location location) {
        super(location);
        this.value = value;
    }

    @Override
    public Cell valueAsCell() {
        return new LongCell(value);
    }

    @Override
    public String displayName() {
        return Long.toString(value);
    }
    
    @Override
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
