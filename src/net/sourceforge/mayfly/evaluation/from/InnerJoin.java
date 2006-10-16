package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.condition.BooleanExpression;

public class InnerJoin extends Join implements FromElement {

    public InnerJoin(FromElement left, FromElement right, BooleanExpression condition) {
        super(left, right, condition);
    }

    public Rows tableContents(DataStore store, String currentSchema) {
        Rows unfiltered = 
            (Rows) left.tableContents(store, currentSchema)
                .cartesianJoin(right.tableContents(store, currentSchema));
        return (Rows) unfiltered.select(condition);
    }

}
