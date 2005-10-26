package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class Equal extends ValueObject implements Selector{
    public static Equal fromEqualTree(Tree equalTree, TreeConverters treeConverters) {
        L both = equalTree.children().convertUsing(treeConverters, new int[0]);

        return new Equal((Transformer)both.get(0), (Transformer) both.get(1));
    }

    private Transformer leftside;
    private Transformer rightside;

    public Equal(Transformer leftside, Transformer rightside) {
        this.leftside = leftside;
        this.rightside = rightside;
    }

    public boolean evaluate(Object candidate) {
        Row row = (Row) candidate;

        return leftside.transform(row).equals(rightside.transform(row));
    }


}
