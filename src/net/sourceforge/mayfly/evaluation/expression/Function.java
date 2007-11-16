package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

public class Function extends Expression {

    public Function(String name, ImmutableList<Expression> arguments,
        Location location, Options options) {
        super(location);
    }

    @Override
    public Cell aggregate(ResultRows rows) {
        throw new UnimplementedException();
    }

    @Override
    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        throw new UnimplementedException();
    }

    @Override
    public boolean sameExpression(Expression other) {
        throw new UnimplementedException();
    }

    @Override
    public String displayName() {
        return "function call";
    }

}
