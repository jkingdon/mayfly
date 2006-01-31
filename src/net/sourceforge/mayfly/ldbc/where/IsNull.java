package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;

public class IsNull extends BooleanExpression {

    private final Expression expression;

    public IsNull(Expression expression) {
        this.expression = expression;
    }

    public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;
        Cell cell = expression.evaluate(row);
        return cell instanceof NullCell;
    }

    public String firstAggregate() {
        return expression.firstAggregate();
    }

}
