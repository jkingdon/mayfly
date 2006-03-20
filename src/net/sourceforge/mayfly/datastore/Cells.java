package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;

import java.util.Iterator;

public class Cells extends Aggregate {
    private L cells;

    public Cells(L headers) {
        this.cells = headers;
    }

    protected Aggregate createNew(Iterable items) {
        return new Cells(new L(items));
    }

    public Iterator iterator() {
        return cells.iterator();
    }

    public String toString() {
        return cells.toString();
    }
}
