package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.ImmutableList;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @internal
 * Does this class need to decide whether it is just a raw set of names,
 * or ones that have somehow been checked for existence and other such
 * things (analogous to 
 * {@link net.sourceforge.mayfly.evaluation.command.UnresolvedTableReference}
 * versus {@link net.sourceforge.mayfly.datastore.TableReference})?
 * 
 * So far there hasn't been a big need for that, but maybe it would
 * make things cleaner (in terms of where to do various checks).
 */
public class ColumnNames implements Iterable<String> {
    
    public static ColumnNames fromColumns(Columns columns) {
        List names = new ArrayList();
        for (Column column : columns) {
            names.add(column.columnName());
        }
        return new ColumnNames(new ImmutableList<String>(names));
    }

    public static ColumnNames singleton(String column) {
        return new ColumnNames(ImmutableList.singleton(column));
    }
    
    /**
     * @internal
     * @param names list of column names, or null to use all columns in
     * the table, in the order specified by the table.
     */
    public static ColumnNames fromParser(
        DataStore store, TableReference table, ImmutableList<String> names) {
        return
            names != null ? 
                new ColumnNames(names) : 
                new ColumnNames(store.table(table).columnNames());
    }

    private final ImmutableList<String> names;

    public ColumnNames(ImmutableList<String> names) {
        this.names = names;
    }
    
    public ImmutableList<String> asList() {
        return names;
    }

    public int size() {
        return names.size();
    }

    public Iterator<String> iterator() {
        return names.iterator();
    }
    
    public String name(int index) {
        return names.get(index);
    }

    public boolean hasColumn(String target) {
        for (String candidate : names) {
            if (candidate.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    public Columns resolve(Columns tableColumns) {
        List resolvedColumns = new ArrayList();
        for (String columnName : names) {
            Column column = tableColumns.columnFromName(columnName);
            resolvedColumns.add(column);
        }
        return new Columns(new ImmutableList(resolvedColumns));
    }

    public void dump(Writer out) throws IOException {
        Iterator<String> iter = iterator();
        while (iter.hasNext()) {
            String column = iter.next();
            out.write(column);
            if (iter.hasNext()) {
                out.write(", ");
            }
        }
    }

    public ColumnNames renameColumn(String oldName, String newName) {
        List result = new ArrayList();
        for (String columnName : names) {
            if (columnName.equalsIgnoreCase(oldName)) {
                result.add(newName);
            }
            else {
                result.add(columnName);
            }
        }
        return new ColumnNames(new ImmutableList(result));
    }

}
