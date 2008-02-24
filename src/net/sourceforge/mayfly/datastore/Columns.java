package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.ImmutableMap;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Columns implements Iterable<Column> {
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


    final ImmutableList columnNames;
    final ImmutableMap nameToColumn;

    public Columns(ImmutableList columns) {
        List names = new ArrayList();
        Map map = new HashMap();
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            CaseInsensitiveString name = column.columnName;
            names.add(name);
            Object oldColumn = map.put(name, column);
            if (oldColumn != null) {
                throw new MayflyException("duplicate column " + name);
            }
        }
        this.columnNames = new ImmutableList(names);
        this.nameToColumn = new ImmutableMap(map);
    }
    
    private Columns(ImmutableList names, ImmutableMap map) {
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

    public ImmutableList asNames() {
        List names = new ArrayList();
        for (int i = 0; i < columnNames.size(); ++i) {
            CaseInsensitiveString column = (CaseInsensitiveString) 
                columnNames.get(i);
            names.add(column.getString());
        }
        return new ImmutableList(names);
    }
    
    public ImmutableList asCaseNames() {
        return columnNames;
    }


    public Column column(int index) {
        return (Column) nameToColumn.get(columnNames.get(index));
    }

    public Column columnFromName(String columnName) {
        return columnFromName(columnName, Location.UNKNOWN);
    }

    public Column columnFromName(String columnName, Location location) {
        Column found = (Column) 
            nameToColumn.get(new CaseInsensitiveString(columnName));
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
        for (Iterator iter = columnNames.iterator(); iter.hasNext();) {
            CaseInsensitiveString existing = (CaseInsensitiveString) 
                iter.next();
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
