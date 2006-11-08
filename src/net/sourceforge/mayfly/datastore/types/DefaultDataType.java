package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Value;

public class DefaultDataType extends DataType {

    private final String dumpName;

    /**
     * @internal
     * For convenience within tests.
     */
    public DefaultDataType() {
        this("undumpable_type");
    }

    public DefaultDataType(String dumpName) {
        this.dumpName = dumpName;
    }

    public Cell coerce(Value value) {
        return value.value;
    }
    
    public String dumpName() {
        return dumpName;
    }

}
