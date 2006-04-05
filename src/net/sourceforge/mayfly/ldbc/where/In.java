package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.expression.literal.Literal;

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
        Cell cell = leftSide.evaluate(row);

        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
			Literal element = (Literal) iter.next();
			if (element.matchesCell(cell)) {
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
