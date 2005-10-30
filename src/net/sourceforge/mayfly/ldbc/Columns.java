package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;

import java.sql.*;
import java.util.*;

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

    /**
     * Only suitable for the case in which we know the column to exist.
     * If the column might not exist, {@link Columns#columnFromName(String)
     * throws a better-worded exception.
     */
    public Column findColumnWithName(String columnNameString) {
        return (Column) find(new HasEquivalentName(columnNameString));
    }


    static class ToName implements Transformer {
        public Object transform(Object from) {
            return ((Column)from).columnName();
        }
    }

    static class HasEquivalentName implements Selector {
        private String columnNameString;

        public HasEquivalentName(String columnNameString) {
            this.columnNameString = columnNameString;
        }

        public boolean evaluate(Object candidate) {
            return candidate.equals(new Column(columnNameString));
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
}
