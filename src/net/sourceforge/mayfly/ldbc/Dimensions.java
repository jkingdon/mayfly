package net.sourceforge.mayfly.ldbc;

import org.ldbc.parser.*;

import java.util.*;
import java.util.List;

public class Dimensions extends Enumerable {

    private List dimensions = new ArrayList();


    public Dimensions() { }

    public Dimensions(List dimensions) {
        this.dimensions = dimensions;
    }


    protected Object createNew(Iterable items) {
        return new Dimensions(asList(items));
    }

    public Iterator iterator() {
        return dimensions.iterator();
    }

    public static Dimensions fromSelectTree(Tree selectTree) {
        Tree.Children tables = selectTree.children().ofType(SQLTokenTypes.SELECTED_TABLE);

        List elements = new ArrayList();

        for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
            Tree table = (Tree) iterator.next();

            Dimension dimension = Dimension.fromSeletedTableTree(table);

            elements.add(dimension);
        }

        return new Dimensions(elements);
    }

    public Dimensions add(Dimension dimension) {
        dimensions.add(dimension);
        return this;
    }
}
