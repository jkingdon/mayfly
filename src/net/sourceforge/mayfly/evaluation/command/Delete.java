package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;

public class Delete extends Command {

    private final UnresolvedTableReference table;
    private final BooleanExpression where;

    public Delete(UnresolvedTableReference table, BooleanExpression where) {
        this.table = table;
        this.where = where;
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        return store.delete(
            table.schema(currentSchema), table.tableName(), where);
    }

}
