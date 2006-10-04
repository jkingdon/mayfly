package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;

import java.util.Iterator;

public class Rows extends Aggregate {
    private final ImmutableList rows;

    public Rows(ImmutableList rows) {
        this.rows = rows;
    }

    public Rows() {
        this(new ImmutableList());
    }

    public Rows(Row row) {
        this(ImmutableList.singleton(row));
    }

    protected Aggregate createNew(Iterable items) {
        return new Rows(new L(items).asImmutable());
    }

    public Iterator iterator() {
        return rows.iterator();
    }
    
    public int size() {
        return rows.size();
    }
    
    public Rows addColumn(Column newColumn) {
        Rows result = new Rows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            result = (Rows) result.with(row.addColumn(newColumn));
        }
        return result;
    }

    public Rows dropColumn(String column) {
        Rows result = new Rows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            result = (Rows) result.with(row.dropColumn(column));
        }
        return result;
    }

}
