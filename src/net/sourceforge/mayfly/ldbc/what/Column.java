package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.datastore.*;


public class Column extends ValueObject implements CellHeader {
    private final String tableOrAlias;
    private final String columnName;
    private final TableIdentifier tableIdentifier;

    public Column(String table, String columnName) {
        this.tableIdentifier = null;
        this.tableOrAlias = table;
        this.columnName = columnName;
    }

    public Column(String column) {
        this((String)null, column);
    }

    public Column(TableIdentifier tableIdentifier, String columnName) {
        this.tableIdentifier = tableIdentifier;
        this.columnName = columnName.toLowerCase();
        this.tableOrAlias = null;
    }

    public String columnName() {
        return columnName;
    }

    public boolean matchesName(String otherName) {
        return matches(null, otherName);
    }

    public boolean matches(String tableOrAlias, String target) {
        if (target.indexOf('.') != -1) {
            throw new MayflyException("column name " + target + " should not contain a period");
        }

        if (tableOrAlias != null && !matchesAliasOrTable(tableOrAlias)) {
            return false;
        }
        return columnName.equalsIgnoreCase(target);
    }

    public boolean matchesAliasOrTable(String tableOrAlias) {
        return tableOrAlias.equalsIgnoreCase(this.tableOrAlias);
    }

    public boolean matches2(String tableName, String columnName) {
        return tableIdentifier.equals(new TableIdentifier(tableName)) 
            && columnName.toLowerCase().equals(this.columnName);
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
