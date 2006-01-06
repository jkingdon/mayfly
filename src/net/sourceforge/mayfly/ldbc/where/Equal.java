package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Equal extends RowExpression {

    public static Equal fromEqualTree(Tree equalTree, TreeConverters treeConverters) {
        L both = equalTree.children().convertUsing(treeConverters);

        return new Equal((Transformer)both.get(0), (Transformer) both.get(1));
    }

    public Equal(Transformer leftSide, Transformer rightSide) {
        super(leftSide, rightSide);
    }

    protected boolean compare(Cell leftSide, Cell rightSide) {
        return leftSide.equals(rightSide);
    }

}
