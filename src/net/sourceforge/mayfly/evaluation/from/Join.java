package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class Join extends ValueObject implements FromElement {

    public final FromElement right;
    public final Condition condition;
    public final FromElement left;

    protected Join(FromElement left, FromElement right, Condition condition) {
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    public ResultRow dummyRow(DataStore store, String currentSchema) {
        ResultRow dummyRow = 
            left.dummyRow(store, currentSchema)
                .combine(right.dummyRow(store, currentSchema));
        condition.evaluate(dummyRow);
        return dummyRow;
    }

}
