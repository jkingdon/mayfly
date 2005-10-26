package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;

public class Gt extends RowExpression {


    public static Gt fromBiggerTree(Tree gtTree, TreeConverters treeConverters) {
        L both = gtTree.children().convertUsing(treeConverters);

        return new Gt((Transformer)both.get(0), (Transformer) both.get(1));
    }


    public Gt(Transformer leftSide, Transformer rightSide) {
        super(leftSide, rightSide);
    }

    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.asLong() > rightSide.asLong();
    }
}
