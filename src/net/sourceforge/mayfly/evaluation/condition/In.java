package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.ImmutableList;

public class In extends Condition {

    public final Expression leftSide;
    public final ImmutableList<Expression> expressions;

    public In(Expression leftSide, ImmutableList<Expression> expressions) {
        this.leftSide = leftSide;
        this.expressions = expressions;
    }

    @Override
    public boolean evaluate(ResultRow row, Evaluator evaluator) {
        Cell leftSideValue = leftSide.evaluate(row, evaluator);

        for (Expression element : expressions) {
            Cell aRightSideValue = element.evaluate(row, evaluator);
            if (leftSideValue.sqlEquals(aRightSideValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String firstAggregate() {
        String firstInLeft = leftSide.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }

        for (Expression element : expressions) {
            String first = element.firstAggregate();
            if (first != null) {
                return first;
            }
        }
        
        return null;
    }
    
    @Override
    public void check(ResultRow row) {
        leftSide.check(row);
        for (Expression expression : expressions) {
            expression.check(row);
        }
    }

}
