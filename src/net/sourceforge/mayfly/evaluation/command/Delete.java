package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.ldbc.where.Where;

public class Delete extends Command {

    private final InsertTable table;
    private final Where where;

    private int rowsAffected;

    public Delete(InsertTable table, Where where) {
        this.table = table;
        this.where = where;
    }

    public DataStore update(DataStore store, String currentSchema) {
        UpdateStore updateResult = store.delete(
            table.schema(currentSchema), table.tableName(), where);
        rowsAffected = updateResult.rowsAffected();
        return updateResult.store();
    }

    public int rowsAffected() {
        return rowsAffected;
    }

}
