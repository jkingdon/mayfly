package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Row extends Aggregate {

    private Tuples tuples;

    public Row(Tuple tuple) {
        this(new Tuples().append(tuple));
    }

    public Row(Tuples tuples) {
        this.tuples = new Tuples(new L(tuples));
    }

    protected Aggregate createNew(Iterable items) {
        return new Row(new Tuples(new L(items)));
    }

    public Iterator iterator() {
        return tuples.iterator();
    }


    public Cell cell(Column column) {
        return cell(column.tableOrAlias(), column.columnName());
    }

    public Cell cell(String tableOrAlias, String column) {
        return tuples.cellFor(findColumn(tableOrAlias, column));
    }

    private Column findColumn(String tableOrAlias, String target) {
        Column found = null;
        for (Iterator iter = tuples.headers().iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(tableOrAlias, target)) {
                if (found != null) {
                    throw new MayflyException("ambiguous column " + target);
                } else {
                    found = column;
                }
            }
        }
        if (found == null) {
            throw new MayflyException("no column " + Column.displayName(tableOrAlias, target));
        } else {
            return found;
        }
    }

    public Columns columns() {
        return new Columns(new ImmutableList(tuples.headers()));
    }

    public String toString() {
        String columns = tuples.headers().toString();
        String cells = tuples.cells().toString();

        return "\n" +
               "Row:\n" +
               "\tcolumns:\t" + columns + "\n" +
               "\tcells:\t" + cells;
    }

    public Tuples tuples() {
        return tuples;
    }
}
