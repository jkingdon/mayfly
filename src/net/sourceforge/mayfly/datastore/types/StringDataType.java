package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Value;

public class StringDataType extends DataType {

    private final String dumpName;

    public StringDataType(String dumpName) {
        this.dumpName = dumpName;
    }

    public Cell coerce(Value value, String columnName) {
        return genericCoerce(value, columnName, "string", StringCell.class);
    }
    
    public String dumpName() {
        return dumpName;
    }

}
