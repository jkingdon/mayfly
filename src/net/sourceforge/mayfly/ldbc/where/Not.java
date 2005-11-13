package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Not extends BooleanExpression {

    public static Not fromNotTree(Tree notTree, TreeConverters converters) {
        L converted = notTree.children().convertUsing(converters);
        return new Not((BooleanExpression) converted.get(0));
    }

    private final BooleanExpression operand;

    public Not(BooleanExpression operand) {
        this.operand = operand;
    }

    public boolean evaluate(Object candidate) {
        return !operand.evaluate(candidate);
    }

    public int parameterCount() {
        return operand.parameterCount();
    }

}
