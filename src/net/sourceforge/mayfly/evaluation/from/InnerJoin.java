package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.condition.Condition;

public class InnerJoin extends Join implements FromElement {

    public InnerJoin(FromElement left, FromElement right, Condition condition) {
        super(left, right, condition);
    }

    public ResultRows tableContents(DataStore store, String currentSchema) {
        ResultRows unfiltered = 
            left.tableContents(store, currentSchema)
                .join(right.tableContents(store, currentSchema));
        return unfiltered.select(condition);
    }

}
