package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class DropTable extends Command {

    private final UnresolvedTableReference table;
    final boolean ifExists;

    public DropTable(UnresolvedTableReference table, boolean ifExists) {
        this.table = table;
        this.ifExists = ifExists;
    }
    
    public UnresolvedTableReference table() {
        return table;
    }

    public UpdateStore update(DataStore store, String schema) {
        if (ifExists) {
            if (!store.hasTable(table, schema)) {
                return new UpdateStore(store, 0);
            }
        }
        TableReference resolved = table.resolve(store, schema, null);
        DataStore newStore = store.dropTable(
            resolved.schema(), resolved.tableName());
        return new UpdateStore(newStore, 0);
    }

}
