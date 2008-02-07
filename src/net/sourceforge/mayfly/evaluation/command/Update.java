package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

import java.util.Iterator;
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

    @Override
    public UpdateStore update(Evaluator evaluator) {
        where.rejectAggregates("UPDATE");
        check();

        DataStore store = evaluator.store();
        String currentSchema = evaluator.currentSchema();

        TableReference resolved = table.resolve(evaluator);
        return store.update(
            table.schema(currentSchema), resolved.tableName(), setClauses, where,
            table.options);
    }

    private void check() {
        for (Iterator iter = setClauses.iterator(); iter.hasNext();) {
            SetClause clause = (SetClause) iter.next();
            clause.rejectAggregates("UPDATE");
        }
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        throw new MayflyInternalException("should call the other update");
    }

}
