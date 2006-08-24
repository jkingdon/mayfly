package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;

public class UnimplementedDataType extends DataType {

    private final String typeName;

    public UnimplementedDataType(String typeName) {
        this.typeName = typeName;
    }

    public Cell coerce(Cell value) {
        throw new UnimplementedException(
            "data type " + typeName + " is not implemented");
    }

}
