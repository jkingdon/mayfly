package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;

import net.sourceforge.mayfly.util.*;
import org.ldbc.parser.*;

import java.util.*;

public class From extends Aggregate {

    private List dimensions = new ArrayList();


    public From() { }

    public From(List dimensions) {
        this.dimensions = dimensions;
    }


    protected Aggregate createNew(Iterable items) {
        return new From(new L().addAll(items));
    }

    public Iterator iterator() {
        return dimensions.iterator();
    }

    public From add(FromElement fromElement) {
        dimensions.add(fromElement);
        return this;
    }

    public L tableNames() {
        return collect(new GetTableName());
    }


    public static From fromSelectTree(Tree selectTree) {
        Tree.Children tables = selectTree.children().ofType(SQLTokenTypes.SELECTED_TABLE);

        List elements = new ArrayList();

        for (Iterator iterator = tables.iterator(); iterator.hasNext();) {
            Tree table = (Tree) iterator.next();

            FromElement fromElement = FromElement.fromSeletedTableTree(table);

            elements.add(fromElement);
        }

        return new From(elements);
    }



    public static class GetTableName implements Transformer {
        public Object transform(Object obj) {
            FromElement f = (FromElement) obj;
            return f.tableName();
        }
    }

    public String singleTableName() {
        if (dimensions.size() != 1) {
            throw new UnimplementedException("expected 1 table, got " + dimensions.size());
        }
        FromElement dimension = (FromElement) dimensions.get(0);
        return dimension.tableName();
    }

}
