package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Row extends Aggregate {

    private final Tuple tuple;

    public Row(TupleElement element) {
        this(new Tuple(element));
    }

    public Row(Tuple tuple) {
        this.tuple = tuple;
    }

    public Row(TupleBuilder tuple) {
        this.tuple = tuple.asTuple();
    }

    protected Aggregate createNew(Iterable items) {
        return new Row(new Tuple(ImmutableList.fromIterable(items)));
    }

    public Iterator iterator() {
        return tuple.iterator();
    }


    public Cell cell(Column column) {
        return cell(column.tableOrAlias(), column.columnName());
    }

    public Cell cell(String tableOrAlias, String column) {
        return tuple.cellFor(findColumn(tableOrAlias, column));
    }

    private Column findColumn(String tableOrAlias, String target) {
        Column found = null;
        for (Iterator iter = tuple.headers().iterator(); iter.hasNext(); ) {
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
        return new Columns(ImmutableList.fromIterable(tuple.headers()));
    }

    public String toString() {
        String columns = tuple.headers().toString();
        String cells = tuple.cells().toString();

        return "\n" +
               "Row:\n" +
               "\tcolumns:\t" + columns + "\n" +
               "\tcells:\t" + cells;
    }

    public Tuple tuple() {
        return tuple;
    }
}
