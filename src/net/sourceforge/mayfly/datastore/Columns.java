package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Columns implements Iterable<Column> {
    public static Columns fromColumnNames(Iterable<String> names) {
        List<Column> columnList = new ArrayList<Column>();
        for (String name : names) {
            columnList.add(new Column(name));
        }

        return new Columns(new ImmutableList(columnList));
    }
    
    public static Columns fromColumnNames(String... names) {
        List<Column> columnList = new ArrayList<Column>();
        for (String name : names) {
            columnList.add(new Column(name));
        }

        return new Columns(new ImmutableList(columnList));
    }

    public static Columns singleton(Column column) {
        return new Columns(
            ImmutableList.fromArray(new Column[] { column }));
    }


    final ImmutableList<CaseInsensitiveString> columnNames;
    final ImmutableMap<CaseInsensitiveString, Column> nameToColumn;

    public Columns(ImmutableList<Column> columns) {
        List names = new ArrayList();
        Map map = new HashMap();
        for (Column column : columns) {
            CaseInsensitiveString name = column.columnName;
            names.add(name);
            Object oldColumn = map.put(name, column);
            if (oldColumn != null) {
                throw new MayflyException("duplicate column " + name);
            }
        }
        this.columnNames = new ImmutableList<CaseInsensitiveString>(names);
        this.nameToColumn = new ImmutableMap(map);
    }
    
    private Columns(ImmutableList<CaseInsensitiveString> names, ImmutableMap map) {
        this.columnNames = names;
        this.nameToColumn = map;
    }
    

    public Iterator iterator() {
        return new Iterator() {

            Iterator delegate = columnNames.iterator();

            public boolean hasNext() {
                return delegate.hasNext();
            }

            public Object next() {
                CaseInsensitiveString name = (CaseInsensitiveString) 
                    delegate.next();
                return nameToColumn.get(name);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        };
    }

    public ImmutableList<String> asNames() {
        List<String> names = new ArrayList<String>();
        for (CaseInsensitiveString column : columnNames) {
            names.add(column.getString());
        }
        return new ImmutableList<String>(names);
    }
    
    public ImmutableList<CaseInsensitiveString> asCaseNames() {
        return columnNames;
    }


    public Column column(int index) {
        return nameToColumn.get(columnNames.get(index));
    }

    public Column columnFromName(String columnName) {
        return columnFromName(columnName, Location.UNKNOWN);
    }

    public Column columnFromName(String columnName, Location location) {
        Column found = nameToColumn.get(new CaseInsensitiveString(columnName));
        if (found == null) {
            throw new NoColumn(columnName, location);
        } else {
            return found;
        }
    }
    
    public boolean hasColumn(String name) {
        return nameToColumn.containsKey(new CaseInsensitiveString(name));
    }

    public Columns replace(Column replacement) {
        return replace(replacement.columnName(), replacement);
    }

    public Columns replace(String existingName, Column replacement) {
        boolean found = false;
        List result = new ArrayList();
        for (Iterator iter = iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(existingName)) {
                result.add(replacement);
                found = true;
            }
            else {
                result.add(column);
            }
        }
        if (!found) {
            throw new NoColumn(existingName);
        }
        return new Columns(new ImmutableList(result));
    }
    
    public Columns with(Column newColumn) {
        return with(newColumn, Position.LAST);
    }

    public Columns with(Column newColumn, Position position) {
        CaseInsensitiveString newName = newColumn.columnName;

        boolean found = false;
        List result = new ArrayList();
        if (position.isFirst()) {
            result.add(newName);
            found = true;
        }
        for (CaseInsensitiveString existing: columnNames) {
            result.add(existing);
            if (position.isAfter(existing.getString())) {
                result.add(newName);
                found = true;
            }
        }
        if (position.isLast()) {
            result.add(newName);
            found = true;
        }
        
        if (!found) {
            throw new NoColumn(position.afterWhat(), position.location());
        }
        
        if (nameToColumn.containsKey(newName)) {
            throw new MayflyException("duplicate column " + newName,
                position.location());
        }

        return new Columns(new ImmutableList(result),
            nameToColumn.with(newName, newColumn));
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
        return columnNames.size();
    }

    public String columnName(int index) {
        return column(index).columnName();
    }

    public CaseInsensitiveString columnCase(int index) {
        return new CaseInsensitiveString(columnName(index));
    }

}
