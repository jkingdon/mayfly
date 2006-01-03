package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class From extends Aggregate {

    private List fromElements = new ArrayList();


    public From() { }

    public From(List fromElements) {
        this.fromElements = fromElements;
    }


    protected Aggregate createNew(Iterable items) {
        return new From(new L().addAll(items));
    }

    public Iterator iterator() {
        return fromElements.iterator();
    }

    public From add(FromElement fromElement) {
        fromElements.add(fromElement);
        return this;
    }


    public static From fromSelectTree(Tree selectTree) {
        Tree.Children tables = selectTree.children().ofType(SQLTokenTypes.SELECTED_TABLE);

        List elements = new ArrayList();

        for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
            Tree table = (Tree) iterator.next();

            FromElement fromElement = FromTable.fromSeletedTableTree(table);

            elements.add(fromElement);
        }

        return new From(elements);
    }



}
