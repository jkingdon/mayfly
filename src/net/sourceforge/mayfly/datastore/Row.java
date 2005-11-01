package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Row extends Aggregate {

    //TODO: will probably need some ordering of columns at some point

    private final ImmutableMap columnToCell;

    public Row(ImmutableMap columnToCell) {
        this.columnToCell = columnToCell;
    }

    protected Aggregate createNew(Iterable items) {
        return new Row(M.fromEntries(items).asImmutable());
    }

    public Iterator iterator() {
        return columnToCell.entrySet().iterator();
    }


    public Cell cell(Column column) {
        return cell(column.tableOrAlias(), column.columnName());
    }

    public Cell cell(String tableOrAlias, String column) {
        return (Cell) columnToCell.get(findColumn(tableOrAlias, column));
    }

    private Column findColumn(String tableOrAlias, String target) {
        Column found = null;
        for (Iterator iter = columnToCell.keySet().iterator(); iter.hasNext(); ) {
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
        return new Columns(new ImmutableList(columnToCell.keySet()));
    }

    public String toString() {
        String columns = columnToCell.keySet().toString();
        String cells = columnToCell.values().toString();

        return "\n" +
               "Row:\n" +
               "\tcolumns:\t" + columns + "\n" +
               "\tcells:\t" + cells;
    }

}
