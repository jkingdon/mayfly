package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class GroupItem {

    private final Expression expression;

    public GroupItem(Expression expression) {
        this.expression = expression;
    }

    public SingleColumn column() {
        if (expression instanceof SingleColumn) {
            return (SingleColumn) expression;
        }
        else {
            throw new MayflyException(
                "GROUP BY expression (as opposed to column) is not implemented");
        }
    }
    
    public Expression expression() {
        return expression;
    }

    public GroupItem resolve(ResultRow row, Evaluator evaluator) {
        Expression resolved = expression.resolve(row, evaluator);
        if (resolved == expression) {
            return this;
        }
        else {
            return new GroupItem(resolved);
        }
    }

}
