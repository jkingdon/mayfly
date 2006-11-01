package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.ImmutableList;

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
public class ColumnNames {
    
    public static ColumnNames fromColumns(Columns columns) {
        List names = new ArrayList();
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            names.add(column.columnName());
        }
        return new ColumnNames(names);
    }

    private final ImmutableList names;

    public ColumnNames(List names) {
        this.names = new ImmutableList(names);
    }

    public int size() {
        return names.size();
    }

    public Iterator iterator() {
        return names.iterator();
    }

    public boolean hasColumn(String target) {
        for (int i = 0; i < names.size(); ++i) {
            String candidate = (String) names.get(i);
            if (candidate.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    public Columns resolve(Columns tableColumns) {
        List resolvedColumns = new ArrayList();
        for (Iterator iter = iterator(); iter.hasNext();) {
            String columnName = (String) iter.next();
            Column column = tableColumns.columnFromName(columnName);
            resolvedColumns.add(column);
        }
        return new Columns(new ImmutableList(resolvedColumns));
    }

}
