package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Value;

/**
 * @internal
 * Data type for a test where the type (and related
 * behavior like coercion of values) is unimportant.
 * 
 * This should just be used in tests.
 */
public class FakeDataType extends DataType {

    @Override
    public Cell coerce(Value value, String columnName) {
        return value.value;
    }
    
    @Override
    public String dumpName() {
        throw new UnimplementedException(
            "specify a real data type if you want to dump the type");
    }

}
