package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.types.DataType;
import net.sourceforge.mayfly.datastore.types.FakeDataType;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.evaluation.expression.DefaultValue;
import net.sourceforge.mayfly.evaluation.expression.SpecifiedDefaultValue;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.CaseInsensitiveString;

public class Column {
    public final CaseInsensitiveString columnName;
    private final DefaultValue defaultValue;
    private final Expression onUpdateValue;
    private final boolean isAutoIncrement;
    private final boolean isSequence;
    public final DataType type;
    
    /** If not-null contraints are managed (e.g. in ALTER TABLE)
     * along with the column, rather than via named constraints,
     * it is more natural to define them here than in a subclass of
     * {@link net.sourceforge.mayfly.datastore.constraint.Constraint}
     */
    public final boolean isNotNull;

    public Column(CaseInsensitiveString name, 
        DefaultValue defaultValue, 
        Expression onUpdateValue, 
        boolean isAutoIncrement,
        boolean isSequence,
        DataType type, boolean isNotNull) {
        this.columnName = name;
        this.defaultValue = defaultValue;
        this.onUpdateValue = onUpdateValue;
        this.isAutoIncrement = isAutoIncrement;
        this.isSequence = isSequence;
        this.type = type;
        this.isNotNull = isNotNull;
        if (isAutoIncrement && isSequence) {
            throw new MayflyInternalException(
                "column " + name + " is both auto increment and sequence");
        }
    }

    public Column(String name, DefaultValue defaultValue, 
        Expression onUpdateValue, 
        boolean isAutoIncrement,
        boolean isSequence,
        DataType type, boolean isNotNull) {
        this(new CaseInsensitiveString(name), defaultValue, onUpdateValue,
            isAutoIncrement, isSequence, type, isNotNull);
    }
    /**
     * Create a column with most of the fields defaulted.  I think this is
     * just called from tests; most code will want to look up an existing
     * column, rather than create one.
     */
    public Column(String columnName) {
        this(columnName, DefaultValue.NOT_SPECIFIED, null, 
            false, false, new FakeDataType(), false);
    }

    public String columnName() {
        return columnName.getString();
    }

    public boolean matches(String target) {
        if (target.indexOf('.') != -1) {
            throw new MayflyException(
                "column name " + target + " should not contain a period");
        }

        return columnName.getString().equalsIgnoreCase(target);
    }

    @Override
    public String toString() {
        return columnName();
    }

    public static String displayName(String tableOrAlias, String column) {
        if (tableOrAlias == null) {
            return column;
        } else {
            return tableOrAlias + "." + column;
        }
    }

    public Cell defaultValue() {
        return defaultValue.cell();
    }

    public String defaultValueAsSql() {
        return defaultValue.asSql();
    }

    /**
     * Mostly called by old code from before when we distinguished
     * between {@link #isSequence()} and {@link #isAutoIncrement()}.
     */
    public boolean isSequenceOrAutoIncrement() {
        return isAutoIncrement || isSequence;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public boolean isSequence() {
        return isSequence;
    }

    public Column afterAutoIncrement(Checker checker, Cell valueInserted,
        boolean isDefault) {
        Cell previousValue;
        if (isDefault && isSequenceOrAutoIncrement()) {
            previousValue = defaultValue();
        }
        else if (isAutoIncrement) {
            if (valueInserted.compareTo(defaultValue()) >= 0) {
                previousValue = valueInserted;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }

        checker.setIdentityValue(previousValue);
        return withIncrementedDefault(previousValue);
    }

    public Column withIncrementedDefault(Cell previousValue) {
        DefaultValue newDefault = 
            new SpecifiedDefaultValue(
                new LongCell(previousValue.asLong() + 1L));
        return new Column(columnName, newDefault, onUpdateValue, 
            isAutoIncrement, isSequence,
            type, isNotNull);
    }
    
    public Column withName(String name) {
        return new Column(name, defaultValue, onUpdateValue, 
            isAutoIncrement, isSequence, type, isNotNull);
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
        if (isNotNull && !defaultValue.isSpecified()) {
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
            return onUpdateValue.evaluate((ResultRow)null);
        }
        else {
            throw new MayflyInternalException(
                "Column " + columnName + " does not have ON UPDATE value");
        }
    }
    
    public String onUpdateValueAsSql() {
        return onUpdateValue.asSql();
    }

    public boolean hasDefault() {
        if (!defaultValue.isSpecified()) {
            return false;
        }
        if (isSequenceOrAutoIncrement()) {
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
