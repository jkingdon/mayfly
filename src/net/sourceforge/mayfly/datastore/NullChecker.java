package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.condition.Condition;

public class NullChecker extends Checker {

    public NullChecker() {
        super(null, null, null);
    }

    public void checkDelete(Row rowToDelete, Row replacementRow) {
    }

    public void checkInsert(Constraints constraints, Row proposedRow) {
    }
    
    public boolean evaluate(Condition condition, Row row, String tableName) {
        // Don't need subselects
        return condition.evaluate(row, tableName);
    }
    
}
