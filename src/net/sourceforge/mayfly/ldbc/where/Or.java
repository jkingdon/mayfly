package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Or extends BooleanExpression {

    public static Or fromOrTree(Tree orTree, TreeConverters converters) {
        L both = orTree.children().convertUsing(converters);
        return new Or((BooleanExpression)both.get(0), (BooleanExpression)both.get(1));
    }

    private BooleanExpression leftSide;
    private BooleanExpression rightSide;

    public Or(BooleanExpression leftSide, BooleanExpression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object row) {
        return leftSide.evaluate(row) || rightSide.evaluate(row);
    }

    public int parameterCount() {
        return leftSide.parameterCount() + rightSide.parameterCount();
    }

    public void substitute(Iterator jdbcParameters) {
        leftSide.substitute(jdbcParameters);
        rightSide.substitute(jdbcParameters);
    }

}
