package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.ldbc.*;

public class NotEq extends RowExpression {
    public static NotEq fromNotEqualTree(Tree notEqualTree, TreeConverters treeConverters) {
        L both = notEqualTree.children().convertUsing(treeConverters);

        return new NotEq((Transformer)both.get(0), (Transformer) both.get(1));
    }


    public NotEq(Transformer leftSide, Transformer rightSide) {
        super(leftSide, rightSide);
    }

    protected boolean compare(Cell leftSide, Cell rightSide) {
        return ! leftSide.equals(rightSide);
    }


}
