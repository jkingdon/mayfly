package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Value;

public abstract class DataType {

    abstract public Cell coerce(Value value, String columnName);

    protected Cell genericCoerce(Value value, String columnName, 
        String typeDescription, Class nativeCellType) {
        if (value.value instanceof NullCell) {
            return value.value;
        }
        else if (value.value.getClass() == nativeCellType) {
            return value.value;
        }
        else {
            throw new MayflyException("attempt to store " + 
                value.value.displayName() + " into " +
                typeDescription + " column " + columnName,
                value.location);
        }
    }

    abstract public String dumpName();

}
