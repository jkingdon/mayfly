package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.Transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Columns extends Aggregate {
    public static Columns fromColumnNames(List columnNameStrings) {
        L columnList =
            new L(columnNameStrings)
                .collect(
                    new Transformer() {
                        public Object transform(Object from) {
                            return new Column((String) from);
                        }
                    }
                );

        return new Columns(columnList.asImmutable());
    }

    public static Columns singleton(Column column) {
        return new Columns(
            ImmutableList.fromArray(new Column[] { column }));
    }


    private final ImmutableList columns;

    public Columns(ImmutableList columns) {
        this.columns = columns;
    }

    protected Aggregate createNew(Iterable items) {
        return new Columns(new L().addAll(items).asImmutable());
    }

    public Iterator iterator() {
        return columns.iterator();
    }

    public L asNames() {
        return collect(new ToName());
    }

    public List asLowercaseNames() {
        return collect(new ToLowercaseName());
    }


    static class ToName implements Transformer {
        public Object transform(Object from) {
            return ((Column)from).columnName();
        }
    }

    static class ToLowercaseName implements Transformer {
        public Object transform(Object from) {
            return ((Column)from).columnName().toLowerCase();
        }
    }

    public ImmutableList asImmutableList() {
        return columns;
    }

    public Column get(int index) {
        return (Column) columns.get(index);
    }

    public Column columnFromName(String columnName) {
        return columnFromName(columnName, Location.UNKNOWN);
    }

    public Column columnFromName(String columnName, Location location) {
        Column found = null;
        for (Iterator iter = columns.iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(columnName)) {
                if (found != null) {
                    throw new MayflyException(
                        "ambiguous column " + columnName, location);
                } else {
                    found = column;
                }
            }
        }
        if (found == null) {
            throw new NoColumn(columnName, location);
        } else {
            return found;
        }
    }
    
    public boolean hasColumn(String name) {
        for (Iterator iter = columns.iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(name)) {
                return true;
            }
        }
        return false;
    }

    public void checkForDuplicates() {
        Set names = new HashSet();
        for (Iterator iter = iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            if (!names.add(column.columnName().toLowerCase())) {
                throw new MayflyException("duplicate column " + column.columnName());
            }
        }
    }

    public Columns replace(Column replacement) {
        boolean found = false;
        List result = new ArrayList();
        for (Iterator iter = iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(replacement.columnName())) {
                result.add(replacement);
                found = true;
            }
            else {
                result.add(column);
            }
        }
        if (!found) {
            throw new NoColumn(replacement.columnName());
        }
        return new Columns(new ImmutableList(result));
    }
    
    public Columns with(Column newColumn) {
        return new Columns(columns.with(newColumn));
    }

    public Columns without(String target) {
        boolean found = false;
        List result = new ArrayList();
        for (Iterator iter = iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(target)) {
                found = true;
            }
            else {
                result.add(column);
            }
        }
        if (found) {
            if (result.isEmpty()) {
                throw new MayflyException("attempt to drop the last column: " + target);
            }
            return new Columns(new ImmutableList(result));
        }
        else {
            throw new NoColumn(target);
        }
    }

}
