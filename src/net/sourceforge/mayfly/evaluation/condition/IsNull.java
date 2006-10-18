package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;

public class IsNull extends BooleanExpression {

    public final Expression expression;

    public IsNull(Expression expression) {
        this.expression = expression;
    }

    public boolean evaluate(ResultRow row) {
        Cell cell = expression.evaluate(row);
        return cell instanceof NullCell;
    }

    public String firstAggregate() {
        return expression.firstAggregate();
    }
    
    public void check(ResultRow row) {
        expression.check(row);
    }

}
