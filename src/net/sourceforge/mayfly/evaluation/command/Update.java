package net.sourceforge.mayfly.evaluation.command;

import java.util.List;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.ldbc.where.Where;

public class Update extends Command {

    private final InsertTable table;
    private final List setClauses;
    private final Where where;

    public Update(InsertTable table, List setClauses, Where where) {
        this.table = table;
        this.setClauses = setClauses;
        this.where = where;
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        return store.update(
            table.schema(currentSchema), table.tableName(), setClauses, where);
    }

}
