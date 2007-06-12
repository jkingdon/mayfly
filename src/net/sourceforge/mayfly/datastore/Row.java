package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @internal
 * Cells for each column.  The order of the columns here is arbitrary.
 * If you want the columns in a predictable order, look up the order
 * in {@link Columns} and then call {@link #cell(String)} for each.
 */
public class Row {

    private final ImmutableList elements;
    
    public Row() {
        this(new ImmutableList());
    }

    public Row(TupleBuilder builder) {
        this(builder.asElements());
    }

    public Row(ImmutableList elements) {
        checkDuplicates(elements);
        this.elements = elements;
    }

    private static void checkDuplicates(ImmutableList elements) {
        Set found = new HashSet();
        for (int i = 0; i < elements.size(); ++i) {
            TupleElement element = (TupleElement) elements.get(i);
            boolean wasPresent = !found.add(element.columnName().toLowerCase());
            if (wasPresent) {
                throw new MayflyInternalException(
                    "duplicate column " + element.columnName());
            }
        }
    }

    public Iterator iterator() {
        return elements.iterator();
    }


    public Cell cell(String column) {
        for (int i = 0; i < elements.size(); ++i) {
            TupleElement element = (TupleElement) elements.get(i);
            if (element.matchesName(column)) {
                return element.cell();
            }
        }
        throw new NoColumn(column);
    }

    /**
     * @internal
     * This method, and the whole concept of having rows refer to
     * columns, is broken.  We don't want to have to re-write all
     * the rows every time that we MODIFY COLUMN or change the
     * auto-increment value (currently stored in the column).
     * Instead, call {@link #columnNames()} and then look up
     * the columns with {@link TableData#findColumn(String)}.
     */
    public Columns columns() {
        throw new MayflyInternalException("Call columnNames instead");
    }
    
    public ImmutableList columnNames() {
        List found = new ArrayList();
        for (int i = 0; i < elements.size(); ++i) {
            TupleElement element = (TupleElement) elements.get(i);
            found.add(element.columnName());
        }
        return new ImmutableList(found);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Row(");
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            TupleElement element = (TupleElement) iter.next();
            result.append(element.columnName());
            result.append("=");
            result.append(element.cell().displayName());
            if (iter.hasNext()) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    public Row addColumn(Column newColumn) {
        return new Row(elements.with(
            new TupleElement(newColumn, newColumn.newColumnValue())));
    }

    public Row dropColumn(String columnName) {
        boolean found = false;
        TupleBuilder newRow = new TupleBuilder();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            TupleElement element = (TupleElement) iter.next();
            if (element.matchesName(columnName)) {
                found = true;
            }
            else {
                newRow.append(element);
            }
        }
        if (found) {
            return newRow.asRow();
        }
        else {
            throw new NoColumn(columnName);
        }
    }

    public Row renameColumn(String oldName, String newName) {
        boolean found = false;
        TupleBuilder newRow = new TupleBuilder();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            TupleElement element = (TupleElement) iter.next();
            if (element.matchesName(oldName)) {
                found = true;
                newRow.append(new TupleElement(newName, element.cell()));
            }
            else {
                newRow.append(element);
            }
        }
        if (found) {
            return newRow.asRow();
        }
        else {
            throw new NoColumn(oldName);
        }
    }

    public int columnCount() {
        return elements.size();
    }

}
