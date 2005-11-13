package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;

public class Where extends BooleanExpression {

    public static final Where EMPTY = new Where(BooleanExpression.TRUE);

    public static Where fromConditionTree(Tree t) {
        return new Where((BooleanExpression) TreeConverters.forWhereTree().transform(new Tree(t.getFirstChild())));
    }


    private BooleanExpression expression = BooleanExpression.TRUE;

    public Where(BooleanExpression expression) {
        this.expression = expression;
    }

    public boolean evaluate(Object candidate) {
        return expression.evaluate(candidate);
    }

    public int parameterCount() {
        return expression.parameterCount();
    }


}
