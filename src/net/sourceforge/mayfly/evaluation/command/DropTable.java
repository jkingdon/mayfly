package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;

public class DropTable extends Command {

    private final String table;
    private final boolean ifExists;

    public DropTable(String table, boolean ifExists) {
        this.table = table;
        this.ifExists = ifExists;
    }
    
    public String table() {
        return table;
    }

    public UpdateStore update(DataStore store, String schema) {
        if (ifExists) {
            if (!store.schema(schema).hasTable(table)) {
                return new UpdateStore(store, 0);
            }
        }
        DataStore newStore = store.dropTable(schema, table);
        return new UpdateStore(newStore, 0);
    }

}
