package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.util.ValueObject;

public class GroupItem extends ValueObject {

    private final Expression expression;

    public GroupItem(Expression expression) {
        this.expression = expression;
    }

    public SingleColumn column() {
        return (SingleColumn) expression;
    }
    
    public Expression expression() {
        return expression;
    }

    public void resolve(Row row) {
        expression.resolve(row);
    }

}
