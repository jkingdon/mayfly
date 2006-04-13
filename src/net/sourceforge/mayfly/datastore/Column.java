package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.ValueObject;

public class Column extends ValueObject implements CellHeader {
    private final String tableOrAlias;
    private final String columnName;
    private final Cell defaultValue;
    private final boolean isAutoIncrement;

    public Column(String table, String name, Cell defaultValue, boolean isAutoIncrement) {
        this.tableOrAlias = table;
        this.columnName = name;
        this.defaultValue = defaultValue;
        this.isAutoIncrement = isAutoIncrement;
    }

    public Column(String table, String columnName) {
        this(table, columnName, NullCell.INSTANCE, false);
    }

    public Column(String column) {
        this((String)null, column);
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

    public Cell defaultValue() {
        return defaultValue;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public Column afterAutoIncrement() {
        Cell newDefault = new LongCell(defaultValue.asLong() + 1L);
        return new Column(tableOrAlias, columnName, newDefault, isAutoIncrement);
    }

}
