package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Where extends ValueObject implements Selector {

    public static final Where EMPTY = new Where(Selector.ALWAYS_TRUE);

    public static Where fromConditionTree(Tree t, TreeConverters treeConverters) {

        //pretty baked.  that's ok at the moment.

        return new Where(Equal.fromEqualTree(new Tree(t.getFirstChild()), treeConverters));
    }


    private Selector expression = Selector.ALWAYS_TRUE;


    public Where(Selector expression) {
        this.expression = expression;
    }

    public boolean evaluate(Object candidate) {
        return expression.evaluate(candidate);
    }


}
