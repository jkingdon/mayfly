package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Value;

public class DefaultDataType extends DataType {

    public Cell coerce(Value value) {
        return value.value;
    }

}
