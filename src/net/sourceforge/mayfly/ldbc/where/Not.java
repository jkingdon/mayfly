package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Not extends ValueObject implements Selector {

    public static Not fromNotTree(Tree notTree, TreeConverters converters) {
        L converted = notTree.children().convertUsing(converters);
        return new Not((Selector) converted.get(0));
    }

    private final Selector operand;

    public Not(Selector operand) {
        this.operand = operand;
    }

    public boolean evaluate(Object candidate) {
        return !operand.evaluate(candidate);
    }

}
