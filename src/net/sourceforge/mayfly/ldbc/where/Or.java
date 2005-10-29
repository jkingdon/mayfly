package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Or extends ValueObject implements Selector {

    public static Or fromOrTree(Tree orTree, TreeConverters converters) {
        L both = orTree.children().convertUsing(converters);
        return new Or((Selector)both.get(0), (Selector)both.get(1));
    }

    private Selector leftSide;
    private Selector rightSide;

    public Or(Selector leftSide, Selector rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object candidate) {
        return leftSide.evaluate(candidate) || rightSide.evaluate(candidate);
    }

}
