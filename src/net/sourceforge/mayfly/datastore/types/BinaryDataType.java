package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.BinaryCell;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Value;

public class BinaryDataType extends DataType {

    public Cell coerce(Value value) {
        if (value.value instanceof BinaryCell) {
            return value.value;
        }
        else {
            return genericCoerce(value, "binary data");
        }
    }

}
