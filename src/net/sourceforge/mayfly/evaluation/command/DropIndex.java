package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class DropIndex extends Command {

    private final UnresolvedTableReference table;
    private final String indexName;

    public DropIndex(UnresolvedTableReference table, String indexName) {
        this.table = table;
        this.indexName = indexName;
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        TableReference reference = table.resolve(store, currentSchema, null);
        DataStore newStore = store.dropIndex(reference, this.indexName);
        return new UpdateStore(newStore, 0);
    }

}
