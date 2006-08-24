package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.Cell;

public class DefaultDataType extends DataType {

    public Cell coerce(Cell value) {
        return value;
    }

}
