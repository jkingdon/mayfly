package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.UnimplementedCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

public class UnimplementedExpression extends Expression {

    private final String expression;

    public UnimplementedExpression(String expression) {
        super(Location.UNKNOWN);
        this.expression = expression;
    }

    public Cell aggregate(Rows rows) {
        return evaluate(null);
    }

    public Cell evaluate(Row row) {
        return new UnimplementedCell(expression);
    }

    public boolean sameExpression(Expression other) {
        return false;
    }

    public String displayName() {
        return "unimplemented";
    }

}
