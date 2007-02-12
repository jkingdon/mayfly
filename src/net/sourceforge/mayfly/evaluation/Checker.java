package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.condition.Condition;

public abstract class Checker {

    public abstract boolean evaluate(Condition condition, Row row, String tableName);

    public abstract Cell newIdentityValue();

    public abstract void setIdentityValue(Cell cell);

    public abstract String schema();

    public abstract DataStore store();

    public abstract void checkDropTable();

    public abstract void checkInsert(Constraints constraints, Row proposedRow);

    public abstract void checkDelete(Row rowToDelete, Row replacementRow);

}
