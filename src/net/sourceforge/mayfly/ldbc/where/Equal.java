package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class Equal extends ValueObject implements Selector{
    public static Equal fromEqualTree(Tree equalTree, TreeConverters treeConverters) {
        L both = equalTree.children().convertUsing(treeConverters, new int[0]);

        return new Equal(both.get(0), both.get(1));
    }

    private Object leftside;
    private Object rightside;

    public Equal(Object leftside, Object rightside) {
        this.leftside = leftside;
        this.rightside = rightside;
    }

    public boolean evaluate(Object candidate) {
        Row r = (Row) candidate;

        return ((Literal)rightside).matchesCell(r.cell((Column) leftside));
    }


}
