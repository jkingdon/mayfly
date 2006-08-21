package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.Checker;

public class NullChecker extends Checker {

    public NullChecker() {
        super(null, null, null);
    }

    public void checkDelete(Row rowToDelete, Row replacementRow) {
    }

    public void checkInsert(Constraints constraints, Row proposedRow) {
    }
    
}
