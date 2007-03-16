package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.M;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Columns {
    public static Columns fromColumnNames(List columnNameStrings) {
        L columnList = new L();
        for (Iterator iter = columnNameStrings.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            columnList.add(new Column(name));
        }

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

    public Iterator iterator() {
        return columns.iterator();
    }

    public ImmutableList asNames() {
        List names = new ArrayList();
        for (int i = 0; i < columns.size(); ++i) {
            Column column = (Column) columns.get(i);
            names.add(column.columnName());
        }
        return new ImmutableList(names);
    }


    public Column column(int index) {
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
        return with(newColumn, Position.LAST);
    }

    public Columns with(Column newColumn, Position position) {
        boolean found = false;
        List result = new ArrayList();
        if (position.isFirst()) {
            result.add(newColumn);
            found = true;
        }
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column existing = (Column) iter.next();
            result.add(existing);
            if (position.isAfter(existing.columnName())) {
                result.add(newColumn);
                found = true;
            }
        }
        if (position.isLast()) {
            result.add(newColumn);
            found = true;
        }
        
        if (!found) {
            throw new NoColumn(position.afterWhat(), position.location());
        }
        return new Columns(new ImmutableList(result));
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

    public int columnCount() {
        return columns.size();
    }

    public M zipper(ValueList values) {
        List keys = columns;
        if (keys.size()!=values.size()) {
            throw new RuntimeException("mapify only supports equal-sized key and value lists. \n" +
                                       "there were (" + keys.size() + " keys and " + values.size() + " values)");
        }

        if (keys.size()!= new HashSet(keys).size()) {
            throw new RuntimeException("mapify only supports unique keysets. \n" +
                                       "keys: " + keys.toString());
        }

        M result = new M();
        for (int i = 0; i < keys.size(); i++) {
            result.put(keys.get(i), values.value(i));
        }

        return result;
    }

    public String columnName(int index) {
        return column(index).columnName();
    }

}
