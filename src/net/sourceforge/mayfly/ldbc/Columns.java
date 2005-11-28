package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Columns extends Aggregate {
    public static Columns fromColumnNames(final String tableName, List columnNameStrings) {
        if (tableName == null) {
            throw new NullPointerException("must pass table to fromColumnNames");
        }

        L columnList =
            new L(columnNameStrings)
                .collect(
                    new Transformer() {
                        public Object transform(Object from) {
                            return new Column(tableName, (String) from);
                        }
                    }
                );

        return new Columns(columnList.asImmutable());
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

    public Column columnMatching(String tableName, String columnName) {
        return (Column) find(new ColumnMatching(tableName, columnName));
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
        return findColumn(null, columnName, columns.iterator());
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


    public static Column findColumn(String tableOrAlias, String columnName, Iterator columnIterator) {
        Column found = null;
        for (Iterator iter = columnIterator; iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(tableOrAlias, columnName)) {
                if (found != null) {
                    throw new MayflyException("ambiguous column " + columnName);
                } else {
                    found = column;
                }
            }
        }
        if (found == null) {
            throw new MayflyException("no column " + Column.displayName(tableOrAlias, columnName));
        } else {
            return found;
        }
    }


    public static class ColumnMatching implements Selector {
        private String tableName;
        private String columnName;

        public ColumnMatching(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }

        public boolean evaluate(Object candidate) {
            Column column = (Column) candidate;

            return column.matches2(tableName, columnName);
        }
    }
}
