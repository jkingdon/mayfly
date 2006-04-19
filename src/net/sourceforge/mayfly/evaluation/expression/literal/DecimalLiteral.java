package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DecimalCell;
import net.sourceforge.mayfly.evaluation.Expression;

import java.math.BigDecimal;

public class DecimalLiteral extends Literal {

    private final BigDecimal value;

    public DecimalLiteral(BigDecimal value) {
        this.value = value;
    }

    public Cell valueAsCell() {
        return new DecimalCell(value);
    }

    public boolean sameExpression(Expression other) {
        if (other instanceof DecimalLiteral) {
            DecimalLiteral decimal = (DecimalLiteral) other;
            return value.equals(decimal.value);
        }
        else {
            return false;
        }
    }

    public String displayName() {
        return value.toString();
    }

}
