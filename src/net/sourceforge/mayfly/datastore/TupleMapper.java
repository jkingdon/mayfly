package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TupleMapper {
    
    private Map columnToCell;

    public TupleMapper() {
        columnToCell = new HashMap();
    }

    public TupleMapper(Row initial) {
        this();
        for (Iterator iter = initial.columnNames(); iter.hasNext();) {
            CaseInsensitiveString name = (CaseInsensitiveString) iter.next();
            columnToCell.put(name, initial.cell(name));
        }
    }

    public void put(Column column, Cell cell) {
        put(column.columnName(), cell);
    }

    public void put(String columnName, Cell cell) {
        put(new CaseInsensitiveString(columnName), cell);
    }

    private Cell put(CaseInsensitiveString column, Cell cell) {
        return (Cell) columnToCell.put(column, cell);
    }

    public void add(String column, Cell cell) {
        add(new CaseInsensitiveString(column), cell);
    }

    public void add(CaseInsensitiveString columnCase, Cell cell) {
        if (put(columnCase, cell) != null) {
            throw new MayflyException(
                "duplicate column " + columnCase);
        }
    }

    public boolean hasColumn(CaseInsensitiveString column) {
        return columnToCell.containsKey(column);
    }

    public Row asRow() {
        return new Row(new ImmutableMap(columnToCell));
    }

}
