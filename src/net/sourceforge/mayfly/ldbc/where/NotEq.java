package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class NotEq {

    public static Not fromNotEqualTree(Tree notEqualTree, TreeConverters treeConverters) {
        L both = notEqualTree.children().convertUsing(treeConverters);

        // Is there a reason to do this rather than Not(Eq(l, r)) ?
        return new Not(new Eq((Transformer)both.get(0), (Transformer) both.get(1)));
    }

}
