package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Iterator;
import java.util.List;

public class In extends Condition {

    public final Expression leftSide;
    public final ImmutableList expressions;

    public In(Expression leftSide, List expressions) {
        this.leftSide = leftSide;
        this.expressions = new ImmutableList(expressions);
    }

    public boolean evaluate(ResultRow row) {
        Cell leftSideValue = leftSide.evaluate(row);

        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            Expression element = (Expression) iter.next();
            Cell aRightSideValue = element.evaluate(row);
            if (leftSideValue.sqlEquals(aRightSideValue)) {
                return true;
            }
        }
        return false;
    }

    public String firstAggregate() {
        String firstInLeft = leftSide.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }

        for (int i = 0; i < expressions.size(); ++i) {
            Expression element = (Expression) expressions.get(i);
            String first = element.firstAggregate();
            if (first != null) {
                return first;
            }
        }
        
        return null;
    }
    
    public void check(ResultRow row) {
        leftSide.check(row);
        for (int i = 0; i < expressions.size(); ++i) {
            Expression expression = (Expression) expressions.get(i);
            expression.check(row);
        }
    }

}
