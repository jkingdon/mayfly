package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

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
    public Condition resolve(ResultRow row, Evaluator evaluator) {
        boolean resolvedSomething = false;
        Expression newLeftSide = leftSide.resolve(row, evaluator);
        if (newLeftSide != leftSide) {
            resolvedSomething = true;
        }
        
        L<Expression> newRightSide = new L<Expression>();
        for (Expression aRightSide : expressions) {
            Expression resolved = aRightSide.resolve(row, evaluator);
            if (resolved != aRightSide) {
                resolvedSomething = true;
            }
            newRightSide.add(resolved);
        }
        
        if (resolvedSomething) {
            return new In(newLeftSide, newRightSide.asImmutable());
        }
        else {
            return this;
        }
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
