package net.sourceforge.mayfly.ldbc.what;


public class Column {
    private final String tableOrAlias;
    private final String columnName;

    public Column(String table, String columnName) {
        this.tableOrAlias = table;
        this.columnName = columnName;
    }

    public Column(String column) {
        this(null, column);
    }
    
    public String columnName() {
        return columnName;
    }

    public boolean matchesName(String otherName) {
        return columnName.equalsIgnoreCase(otherName);
    }

    public boolean equals(Object other) {
        // TODO: This is broken.  If other isn't a column, should be a
        // false return, not ClassCastException
        String otherName = ((Column)other).columnName;
        return matchesName(otherName);
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return columnName;
    }

    public String tableOrAlias() {
        return tableOrAlias;
    }

}
