package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.util.*;


public class Column extends ValueObject {
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

    public boolean matches(String tableOrAlias, String target) {
        if (tableOrAlias != null && !tableOrAlias.equalsIgnoreCase(this.tableOrAlias)) {
            return false;
        }
        return matchesName(target);
    }

    public String tableOrAlias() {
        return tableOrAlias;
    }

    public String toString() {
        return displayName(tableOrAlias, columnName);
    }

    public static String displayName(String tableOrAlias, String column) {
        if (tableOrAlias == null) {
            return column;
        } else {
            return tableOrAlias + "." + column;
        }
    }
    
}
