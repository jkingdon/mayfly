package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.condition.Condition;

public class NullChecker extends Checker {

    @Override
    public void checkDelete(Row rowToDelete, Row replacementRow) {
    }

    @Override
    public void checkInsert(Constraints constraints, Row proposedRow) {
    }
    
    @Override
    public boolean evaluate(Condition condition, Row row, String tableName) {
        // Don't need subselects
        return condition.evaluate(row, tableName);
    }

    @Override
    public void checkDropTable() {
        throw new UnimplementedException();
    }

    @Override
    public Cell newIdentityValue() {
        throw new UnimplementedException();
    }

    @Override
    public String schema() {
        throw new UnimplementedException();
    }

    @Override
    public void setIdentityValue(Cell cell) {
        throw new UnimplementedException();
    }

    @Override
    public DataStore store() {
        throw new UnimplementedException();
    }

}
