package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Value;

public class IntegerDataType extends DataType {

    private final String dumpName;

    /**
     * @internal
     * For convenience within tests.
     */
    public IntegerDataType() {
        this("undumpable_type");
    }

    public IntegerDataType(String dumpName) {
        this.dumpName = dumpName;
    }

    public Cell coerce(Value value) {
        return genericCoerce(value, dumpName.toLowerCase(), LongCell.class);
    }
    
    public String dumpName() {
        return dumpName;
    }

}
