package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Where extends ValueObject implements Selector {

    public static Where fromConditionTree(Tree t) {

        //pretty baked.  that's ok at the moment.

        return
            new Where()
                .add(Equal.fromTree(new Tree(t.getFirstChild())));
    }


    private Selector expression = Selector.ALWAYS_TRUE;


    //TODO: get rid of this
    public Where add(Equal eq) {
        expression = eq;
        return this;
    }

    public boolean evaluate(Object candidate) {
        return expression.evaluate(candidate);
    }


}
