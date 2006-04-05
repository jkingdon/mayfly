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

    public DataStore update(DataStore store, String schema) {
        if (ifExists) {
            if (!store.schema(schema).hasTable(table)) {
                return store;
            }
        }
        return store.dropTable(schema, table);
    }

    public int rowsAffected() {
        return 0;
    }

}
