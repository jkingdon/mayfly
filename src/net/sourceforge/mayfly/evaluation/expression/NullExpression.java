package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;

public class NullExpression extends Expression {

    public NullExpression(Location location) {
        super(location);
    }

    @Override
    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        return NullCell.INSTANCE;
    }

    @Override
    public Cell aggregate(ResultRows rows) {
        return NullCell.INSTANCE;
    }

    @Override
    public boolean sameExpression(Expression other) {
        return other instanceof NullExpression;
    }

    @Override
    public String displayName() {
        return "null";
    }

}
