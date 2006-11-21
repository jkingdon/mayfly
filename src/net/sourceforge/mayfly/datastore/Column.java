package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.types.DataType;
import net.sourceforge.mayfly.datastore.types.DefaultDataType;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.parser.Location;

public class Column {
    private final String columnName;
    private final Cell defaultValue;
    private final Cell onUpdateValue;
    private final boolean isAutoIncrement;
    public final DataType type;
    
    /** We'll probably want not-null contraints to be implemented
     * via a subclass of
     * {@link net.sourceforge.mayfly.datastore.constraint.Constraint}
     * once we implement named constraints.  But we can
     * worry about that then.
     */
    public final boolean isNotNull;

    public Column(String name, Cell defaultValue, Cell onUpdateValue, 
        boolean isAutoIncrement,
        DataType type, boolean isNotNull) {
        this.columnName = name;
        this.defaultValue = defaultValue;
        this.onUpdateValue = onUpdateValue;
        this.isAutoIncrement = isAutoIncrement;
        this.type = type;
        this.isNotNull = isNotNull;
    }

    /**
     * Create a column with most of the fields defaulted.  I think this is
     * just called from tests; most code will want to look up an existing
     * column, rather than create one.
     */
    public Column(String columnName) {
        this(columnName, NullCell.INSTANCE, null, 
            false, new DefaultDataType(), false);
    }

    public String columnName() {
        return columnName;
    }

    public boolean matches(String target) {
        if (target.indexOf('.') != -1) {
            throw new MayflyException(
                "column name " + target + " should not contain a period");
        }

        return columnName.equalsIgnoreCase(target);
    }

    public String toString() {
        return columnName;
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

    public Column afterAutoIncrement(Checker checker) {
        Cell valueJustInserted = defaultValue;
        Cell newDefault = new LongCell(valueJustInserted.asLong() + 1L);
        checker.setIdentityValue(valueJustInserted);
        return new Column(columnName, newDefault, onUpdateValue, isAutoIncrement,
            type, isNotNull);
    }

    /**
     * Coerce to the type of this column.
     */
    public Cell coerce(Cell value, Location location) {
        if (isNotNull && value instanceof NullCell) {
            throw new MayflyException(
                "column " + columnName() + " cannot be null",
                location);
        }
        return type.coerce(new Value(value, location), columnName());
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

    public boolean hasDefault() {
        if (defaultValue() instanceof NullCell) {
            return false;
        }
        if (isAutoIncrement) {
            /* We implement the next value to be assigned as
               a default value.  At least for now, we dump it
               that way too.  But don't dump it if leaving it out
               has the same meaning (that is, next value is 1). */
            if (defaultValue.sqlEquals(new LongCell(1))) {
                return false;
            }
        }
        return true;
    }

}
