package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class And extends ValueObject implements Selector {
    private Selector leftSide;
    private Selector rightSide;


    public static And fromAndTree(Tree andTree, TreeConverters treeConverters) {
        L both = andTree.children().convertUsing(treeConverters);
        return new And((Selector)both.get(0), (Selector)both.get(1));
    }


    public And(Selector leftSide, Selector rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object candidate) {
        return leftSide.evaluate(candidate) && rightSide.evaluate(candidate);
    }
}
