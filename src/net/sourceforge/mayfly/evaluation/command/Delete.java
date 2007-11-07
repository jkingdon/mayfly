package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public class Delete extends Command {

    private final UnresolvedTableReference table;
    private final Condition where;

    public Delete(UnresolvedTableReference table, Condition where) {
        this.table = table;
        this.where = where;
    }

    @Override
    public UpdateStore update(Evaluator evaluator) {
        where.rejectAggregates("DELETE");
        DataStore store = evaluator.store();
        String currentSchema = evaluator.currentSchema();
        TableReference resolved = 
            table.resolve(store, currentSchema, null);
        return store.delete(
            resolved.schema(), resolved.tableName(), where,
            table.options);
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        throw new MayflyInternalException("should call the other update");
    }
}
