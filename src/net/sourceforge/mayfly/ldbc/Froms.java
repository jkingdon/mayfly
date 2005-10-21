package net.sourceforge.mayfly.ldbc;

import org.ldbc.parser.*;

import java.util.*;
import java.util.List;

public class Froms extends Enumerable {

    private List dimensions = new ArrayList();


    public Froms() { }

    public Froms(List dimensions) {
        this.dimensions = dimensions;
    }


    protected Object createNew(Iterable items) {
        return new Froms(asList(items));
    }

    public Iterator iterator() {
        return dimensions.iterator();
    }

    public static Froms fromSelectTree(Tree selectTree) {
        Tree.Children tables = selectTree.children().ofType(SQLTokenTypes.SELECTED_TABLE);

        List elements = new ArrayList();

        for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
            Tree table = (Tree) iterator.next();

            From from = From.fromSeletedTableTree(table);

            elements.add(from);
        }

        return new Froms(elements);
    }

    public Froms add(From from) {
        dimensions.add(from);
        return this;
    }
}
