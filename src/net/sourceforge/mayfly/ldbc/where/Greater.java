package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Greater extends RowExpression {

    public static Greater fromBiggerTree(Tree gtTree, TreeConverters treeConverters) {
        L both = gtTree.children().convertUsing(treeConverters);

        return new Greater((Transformer)both.get(0), (Transformer) both.get(1));
    }

    public static Greater fromSmallerTree(Tree ltTree, TreeConverters treeConverters) {
        L both = ltTree.children().convertUsing(treeConverters);

        return new Greater((Transformer)both.get(1), (Transformer) both.get(0));
    }


    public Greater(Transformer leftSide, Transformer rightSide) {
        super(leftSide, rightSide);
    }

    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.asLong() > rightSide.asLong();
    }

}
