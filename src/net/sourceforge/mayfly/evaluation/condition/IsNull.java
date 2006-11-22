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

    public boolean evaluate(ResultRow row, Evaluator evaluator) {
        Cell cell = expression.evaluate(row, evaluator);
        return cell instanceof NullCell;
    }

    public String firstAggregate() {
        return expression.firstAggregate();
    }
    
    public void check(ResultRow row) {
        expression.check(row);
    }

}
