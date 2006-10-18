package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.condition.Condition;

import java.util.List;

public class Update extends Command {

    private final UnresolvedTableReference table;
    private final List setClauses;
    private final Condition where;

    public Update(UnresolvedTableReference table, List setClauses, Condition where) {
        this.table = table;
        this.setClauses = setClauses;
        this.where = where;
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        return store.update(
            table.schema(currentSchema), table.tableName(), setClauses, where);
    }

}
