package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

public class CellExpression extends Literal {

    private final Cell value;

    public CellExpression(Cell value) {
        this(value, Location.UNKNOWN);
    }

    public CellExpression(Cell value, Location location) {
        super(location);
        this.value = value;
    }

    public boolean sameExpression(Expression other) {
//        return false;
        throw new UnimplementedException();
    }

    public String displayName() {
        return value.displayName();
    }

    public Cell valueAsCell() {
        return value;
    }

}
