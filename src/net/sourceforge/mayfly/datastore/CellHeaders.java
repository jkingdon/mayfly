package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.ldbc.Columns;
import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.Selector;

import java.util.Iterator;

public class CellHeaders extends Aggregate {
    private L headers;

    public CellHeaders(L headers) {
        this.headers = headers;
    }

    protected Aggregate createNew(Iterable items) {
        return new CellHeaders(new L(items));
    }

    public Iterator iterator() {
        return headers.iterator();
    }

    public String toString() {
        return headers.toString();
    }

    public Columns thatAreColumns() {
        return new Columns(select(new IsColumn()).asList().asImmutable());
    }

    public static class IsColumn implements Selector {
        public boolean evaluate(Object candidate) {
            return candidate instanceof Column;
        }
    }
}
