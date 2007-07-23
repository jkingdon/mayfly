package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableMap;

import java.util.Iterator;

/**
 * @internal
 * Cells for each column.  The order of the columns here is arbitrary.
 * If you want the columns in a predictable order, look up the order
 * in {@link Columns} and then call {@link #cell(String)} for each.
 */
public class Row {

    final ImmutableMap cells;
    
    public Row() {
        this(new ImmutableMap());
    }

    public Row(ImmutableMap namesToCells) {
        this.cells = namesToCells;
    }

    public Iterator columnNames() {
        return cells.keySet().iterator();
    }


    public Cell cell(CaseInsensitiveString column) {
        Cell cell = (Cell) cells.get(column);
        if (cell == null) {
            throw new NoColumn(column);
        }
        return cell;
    }

    public Cell cell(String column) {
        return cell(new CaseInsensitiveString(column));
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Row(");
        for (Iterator iter = cells.keySet().iterator(); iter.hasNext();) {
            CaseInsensitiveString column = (CaseInsensitiveString) iter.next();
            result.append(column);
            result.append("=");
            result.append(cell(column).displayName());
            if (iter.hasNext()) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    public Row addColumn(Column newColumn) {
        Cell value = newColumn.newColumnValue();
        CaseInsensitiveString name = newColumn.columnName;
        if (cells.containsKey(name)) {
            throw new MayflyException("duplicate column " + name);
        }
        return new Row(cells.add(name, value));
    }

    public Row dropColumn(String columnName) {
        CaseInsensitiveString name = new CaseInsensitiveString(columnName);
        if (!cells.containsKey(name)) {
            throw new NoColumn(name);
        }
        return new Row(cells.without(name));
    }

    public Row renameColumn(String oldName, String newName) {
        CaseInsensitiveString oldCase = new CaseInsensitiveString(oldName);
        CaseInsensitiveString newCase = new CaseInsensitiveString(newName);
        if (!cells.containsKey(oldCase)) {
            throw new NoColumn(oldCase);
        }
        if (cells.containsKey(newCase)) {
            throw new MayflyException("duplicate column " + newCase);
        }
        Cell currentValue = cell(oldCase);
        return new Row(cells.without(oldCase).with(newCase, currentValue));
    }

    public int columnCount() {
        return cells.size();
    }

}
