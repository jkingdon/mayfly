package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Where extends ValueObject {

    private L expressions = new L();

    public Where add(Equal eq) {
        expressions.add(eq);
        return this;
    }


    public static Where fromConditionTree(Tree t) {

        //pretty baked.  that's ok at the moment.

        return
            new Where()
                .add(Equal.fromTree(new Tree(t.getFirstChild())));
    }

}
