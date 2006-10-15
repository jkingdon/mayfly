package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Rows;
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

    public Rows dummyRows(DataStore store, String currentSchema) {
        Rows dummyRows = 
            (Rows) left.dummyRows(store, currentSchema)
                .cartesianJoin(right.dummyRows(store, currentSchema));
        dummyRows.select(condition);
        return dummyRows;
    }
    
    public FromElement right() {
        return right;
    }
    
    public FromElement left() {
        return left;
    }

}
