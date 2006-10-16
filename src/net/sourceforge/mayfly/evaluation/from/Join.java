package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.util.ValueObject;

public abstract class Join extends ValueObject implements FromElement {

    protected final FromElement right;
    protected final BooleanExpression condition;
    protected final FromElement left;

    protected Join(FromElement left, FromElement right, BooleanExpression condition) {
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    public Row dummyRow(DataStore store, String currentSchema) {
        Row dummyRow = 
            (Row) left.dummyRow(store, currentSchema)
                .plus(right.dummyRow(store, currentSchema));
        condition.evaluate(dummyRow);
        return dummyRow;
    }
    
    public FromElement right() {
        return right;
    }
    
    public FromElement left() {
        return left;
    }

}
