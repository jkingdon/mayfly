package net.sourceforge.mayfly.ldbc.where;

import java.util.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class And extends BooleanExpression {
    private BooleanExpression leftSide;
    private BooleanExpression rightSide;


    public static And fromAndTree(Tree andTree, TreeConverters treeConverters) {
        L both = andTree.children().convertUsing(treeConverters);
        return new And((BooleanExpression)both.get(0), (BooleanExpression)both.get(1));
    }


    public And(BooleanExpression leftSide, BooleanExpression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object row) {
        return leftSide.evaluate(row) && rightSide.evaluate(row);
    }

    public int parameterCount() {
        return leftSide.parameterCount() + rightSide.parameterCount();
    }

    public void substitute(Iterator jdbcParameters) {
        leftSide.substitute(jdbcParameters);
        rightSide.substitute(jdbcParameters);
    }

}
