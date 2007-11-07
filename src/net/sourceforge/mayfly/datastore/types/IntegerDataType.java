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

    @Override
    public Cell coerce(Value value, String columnName) {
        return genericCoerce(value, columnName, dumpName.toLowerCase(), 
            LongCell.class);
    }
    
    @Override
    public String dumpName() {
        return dumpName;
    }

}
