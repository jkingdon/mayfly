package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

import java.sql.*;
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

    /**
     * Only suitable for the case in which we know the column to exist.
     * If the column might not exist, {@link Columns#columnFromName(String)
     * throws a better-worded exception.
     */
    public Column findColumnWithName(String columnNameString) {
        return (Column) find(new HasEquivalentName(columnNameString));
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

    static class HasEquivalentName implements Selector {
        private String columnNameString;

        public HasEquivalentName(String columnNameString) {
            this.columnNameString = columnNameString;
        }

        public boolean evaluate(Object candidate) {
            Column column = (Column) candidate;
            return column.matchesName(columnNameString);
        }
    }

    public ImmutableList asImmutableList() {
        return columns;
    }

    public Column get(int index) {
        return (Column) columns.get(index);
    }

    public Column columnFromName(String columnName) throws SQLException {
        for (int i = 0; i < size(); ++i) {
            Column column = get(i);
            if (column.matchesName(columnName)) {
                return column;
            }
        }
        throw new SQLException("no column " + columnName);
    }

    public static class ColumnMatching implements Selector{
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
