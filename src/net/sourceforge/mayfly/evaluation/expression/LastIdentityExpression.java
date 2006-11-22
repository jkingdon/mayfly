package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.command.LastIdentity;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class LastIdentityExpression extends Expression {

    public Cell aggregate(ResultRows rows) {
        throw new MayflyInternalException(
            "no need to aggregate with identity()");
    }

    /**
     * @internal
     * At least the way things are set up now, the whole
     * concept of evaluating identity() doesn't make any sense;
     * we merely look up identity() in a row which we create
     * over in {@link LastIdentity}.
     * 
     * This probably changes if we try to support
     * "call identity() + 7" or some such, but I don't even know
     * whether that makes any sense to worry about.
     */
    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        throw new MayflyInternalException(
            "no need to evaluate with identity()");
    }

    public boolean sameExpression(Expression other) {
        return other instanceof LastIdentityExpression;
    }

    public String displayName() {
        return "identity()";
    }

}
