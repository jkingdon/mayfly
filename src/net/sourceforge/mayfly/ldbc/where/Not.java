package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Not extends BooleanExpression {

    public static Not fromNotTree(Tree notTree, TreeConverters converters) {
        L converted = notTree.children().convertUsing(converters);
        Object operand = converted.get(0);
        if (operand instanceof BooleanExpression) {
            return new Not((BooleanExpression) operand);
        } else {
            throw new MayflyException("operand of NOT must be a boolean expression");
        }
    }

    private final BooleanExpression operand;

    public Not(BooleanExpression operand) {
        this.operand = operand;
    }

    public boolean evaluate(Object row) {
        return !operand.evaluate(row);
    }

    public int parameterCount() {
        return operand.parameterCount();
    }

    public void substitute(Iterator jdbcParameters) {
        operand.substitute(jdbcParameters);
    }

}
