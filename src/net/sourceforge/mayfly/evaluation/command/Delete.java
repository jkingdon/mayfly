package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.condition.Condition;

public class Delete extends Command {

    private final UnresolvedTableReference table;
    private final Condition where;

    public Delete(UnresolvedTableReference table, Condition where) {
        this.table = table;
        this.where = where;
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        return store.delete(
            table.schema(currentSchema), table.tableName(), where);
    }

}
