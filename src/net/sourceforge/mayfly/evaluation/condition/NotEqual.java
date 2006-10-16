package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.evaluation.Expression;

public class NotEqual {

    public static BooleanExpression construct(Expression left, Expression right) {
        // Not sure we can handle nulls right by doing it this way.
        // Certainly we aren't dealing with nulls correctly yet.
        return new Not(new Equal(left, right));
    }

}
