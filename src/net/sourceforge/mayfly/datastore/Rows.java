package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Iterator;

public class Rows {
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

    public Iterator iterator() {
        return rows.iterator();
    }
    
    public Row row(int index) {
        return (Row) rows.get(index);
    }

    public int rowCount() {
        return rows.size();
    }
    
    public Rows with(Row newRow) {
        return new Rows(rows.with(newRow));
    }
    
    public Rows addColumn(Column newColumn) {
        Rows result = new Rows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            result = result.with(row.addColumn(newColumn));
        }
        return result;
    }

    public Rows dropColumn(String column) {
        Rows result = new Rows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            result = result.with(row.dropColumn(column));
        }
        return result;
    }

}
