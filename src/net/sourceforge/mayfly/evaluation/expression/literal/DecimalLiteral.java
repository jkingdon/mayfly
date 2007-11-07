package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DecimalCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

import java.math.BigDecimal;

public class DecimalLiteral extends Literal {

    public final BigDecimal value;

    public DecimalLiteral(String value) {
        this(value, Location.UNKNOWN);
    }

    public DecimalLiteral(String value, Location location) {
        this(new BigDecimal(value), location);
    }

    public DecimalLiteral(BigDecimal value, Location location) {
        super(location);
        this.value = value;
    }

    @Override
    public Cell valueAsCell() {
        return new DecimalCell(value);
    }

    @Override
    public boolean sameExpression(Expression other) {
        if (other instanceof DecimalLiteral) {
            DecimalLiteral decimal = (DecimalLiteral) other;
            return value.equals(decimal.value);
        }
        else {
            return false;
        }
    }

    @Override
    public String displayName() {
        return value.toString();
    }

}
