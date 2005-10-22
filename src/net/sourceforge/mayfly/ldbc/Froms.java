package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;

import net.sourceforge.mayfly.util.*;
import org.ldbc.parser.*;

import java.util.*;

public class Froms extends Aggregate {

    private List dimensions = new ArrayList();


    public Froms() { }

    public Froms(List dimensions) {
        this.dimensions = dimensions;
    }


    protected Aggregate createNew(Iterable items) {
        return new Froms(new L().slurp(items));
    }

    public Iterator iterator() {
        return dimensions.iterator();
    }

    public Froms add(From from) {
        dimensions.add(from);
        return this;
    }

    public L tableNames() {
        return collect(new GetTableName());
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



    public static class GetTableName implements Transformer {
        public Object transform(Object obj) {
            From f = (From) obj;
            return f.tableName();
        }
    }

    public String singleTableName() {
        if (dimensions.size() != 1) {
            throw new UnimplementedException("expected 1 table, got " + dimensions.size());
        }
        From dimension = (From) dimensions.get(0);
        return dimension.tableName();
    }

}
