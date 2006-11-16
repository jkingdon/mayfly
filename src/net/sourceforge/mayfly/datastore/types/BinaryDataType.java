package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.BinaryCell;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Value;

public class BinaryDataType extends DataType {

    private static final int NO_SIZE = -1;
    private final long size;

    public BinaryDataType() {
        this(NO_SIZE);
    }

    public BinaryDataType(long size) {
        this.size = size;
    }

    public Cell coerce(Value value, String columnName) {
        return genericCoerce(value, columnName, "binary", BinaryCell.class);
    }
    
    public String dumpName() {
        if (hasSize()) {
            return "BLOB(" + size() + ")";
        }
        else {
            return "BLOB";
        }
    }

    private long size() {
        if (hasSize()) {
            return size;
        }
        else {
            throw new IllegalStateException("Asked for size without one");
        }
    }

    private boolean hasSize() {
        return size != NO_SIZE;
    }

}
