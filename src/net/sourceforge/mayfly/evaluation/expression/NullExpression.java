package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.parser.Location;

public class NullExpression extends Expression {

    public NullExpression(Location location) {
        super(location);
    }

    public Cell evaluate(ResultRow row) {
        return NullCell.INSTANCE;
    }

    public Cell aggregate(ResultRows rows) {
        return NullCell.INSTANCE;
    }

    public boolean sameExpression(Expression other) {
        return other instanceof NullExpression;
    }

    public String displayName() {
        return "null";
    }

}
