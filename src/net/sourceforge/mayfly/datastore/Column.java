package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.types.DataType;
import net.sourceforge.mayfly.datastore.types.DefaultDataType;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ValueObject;

public class Column extends ValueObject implements CellHeader {
    private final String tableOrAlias;
    private final String columnName;
    private final Cell defaultValue;
    private final Cell onUpdateValue;
    private final boolean isAutoIncrement;
    private final DataType type;
    
    /** This will probably want to be a subclass of
     * {@link net.sourceforge.mayfly.datastore.constraint.Constraint}
     * once we implement named constraints.  But we can
     * worry about that then.
     */
    private final boolean isNotNull;

    public Column(String table, String name, Cell defaultValue, Cell onUpdateValue,
        boolean isAutoIncrement, DataType type, boolean isNotNull) {
        this.tableOrAlias = table;
        this.columnName = name;
        this.defaultValue = defaultValue;
        this.onUpdateValue = onUpdateValue;
        this.isAutoIncrement = isAutoIncrement;
        this.type = type;
        this.isNotNull = isNotNull;
    }

    public Column(String table, String columnName) {
        this(table, columnName, NullCell.INSTANCE, null, false, new DefaultDataType(), false);
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
        return new Column(tableOrAlias, columnName, newDefault, onUpdateValue,
            isAutoIncrement, type, isNotNull);
    }

    /**
     * Coerce to the type of this column.
     */
    public Cell coerce(Cell value) {
        return type.coerce(value);
    }

    /**
     * Check not null constraint.  Maybe could be combined with
     * coerce?
     */
    public void check(Cell proposedCell, Location location) {
        if (isNotNull && proposedCell instanceof NullCell) {
            throw new MayflyException(
                "column " + columnName() + " cannot be null",
                location);
        }
    }

    public Cell newColumnValue() {
        if (isNotNull && defaultValue instanceof NullCell) {
            throw new MayflyException(
                "no default value for column " + columnName(),
                Location.UNKNOWN);
        }
        return defaultValue();
    }

    public boolean hasOnUpdateValue() {
        return onUpdateValue != null;
    }

    public Cell getOnUpdateValue() {
        if (hasOnUpdateValue()) {
            return onUpdateValue;
        }
        else {
            throw new MayflyInternalException(
                "Column " + columnName + " does not have ON UPDATE value");
        }
    }

}
