package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Row extends Aggregate {

    //TODO: will probably need some ordering of cols at some point

    private ImmutableMap columnToCell;

    public Row(ImmutableMap columnToCell) {
        this.columnToCell = columnToCell;
    }

    protected Aggregate createNew(Iterable items) {
        return new Row(M.fromEntries(items.iterator()).asImmutable());
    }

    public Iterator iterator() {
        return columnToCell.entrySet().iterator();
    }


    public Cell cell(Column column) {
        return (Cell) columnToCell.get(column);
    }

    public Columns columns() {
        return new Columns(new ArrayList(columnToCell.keySet()));
    }
}
