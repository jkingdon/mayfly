package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class DataType extends ValueObject {

    abstract public Cell coerce(Value value);

    protected Cell genericCoerce(Value value, String typeDescription) {
        if (value.value instanceof NullCell) {
            return value.value;
        }
        else {
            throw new MayflyException("attempt to store " + 
                value.value.displayName() + " as " +
                        typeDescription,
                value.location);
        }
    }

}
