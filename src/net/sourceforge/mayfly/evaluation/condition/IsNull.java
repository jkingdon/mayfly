package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class IsNull extends Condition {

    public final Expression expression;

    public IsNull(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean evaluate(ResultRow row, Evaluator evaluator) {
        Cell cell = expression.evaluate(row, evaluator);
        return cell instanceof NullCell;
    }
    
    @Override
    public Condition resolve(ResultRow row, Evaluator evaluator) {
        Expression resolved = expression.resolve(row, evaluator);
        if (resolved != expression) {
            return new IsNull(resolved);
        }
        else {
            return this;
        }
    }

    @Override
    public String firstAggregate() {
        return expression.firstAggregate();
    }
    
    @Override
    public void check(ResultRow row) {
        expression.check(row);
    }

}
