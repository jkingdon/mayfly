package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

public class OrderItem extends ValueObject {

    public static OrderItem fromTree(Tree tree, TreeConverters converters) {
        Tree.Children children = tree.children();
        SingleColumn column = (SingleColumn) converters.transform((Tree) children.element(0));
        boolean ascending = true;
        if (children.size() > 1) {
            Tree direction = (Tree) children.element(1);
            if (direction.getType() == SQLTokenTypes.LITERAL_desc) {
                ascending = false;
            }
        }
        return new OrderItem(column, ascending);
    }

    private final SingleColumn column;
    private final boolean ascending;

    public OrderItem(SingleColumn column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    public int compareRows(Row first, Row second) {
        Cell cell1 = (Cell) column.transform(first);
        Cell cell2 = (Cell) column.transform(second);
        int comparison = cell1.compareTo(cell2);
        return ascending ? comparison : - comparison;
    }

    public void check(Row dummyRow) {
        column.transform(dummyRow);
    }

}
