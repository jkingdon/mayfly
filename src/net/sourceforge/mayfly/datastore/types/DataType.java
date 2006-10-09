package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class DataType extends ValueObject {

    abstract public Cell coerce(Value value);

}
