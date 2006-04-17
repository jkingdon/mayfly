package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;

import java.util.Iterator;
import java.util.List;

public class In extends BooleanExpression {

    private Expression leftSide;
    private List expressions;

    public In(Expression leftSide, List expressions) {
        this.leftSide = leftSide;
        this.expressions = expressions;
    }

    public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;
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

}
