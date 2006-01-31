package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.DataStore;

public class DropTable extends Command {

    private final String table;

    public DropTable(String table) {
        this.table = table;
    }
    
    public String table() {
        return table;
    }

    public DataStore update(DataStore store, String schema) {
        return store.dropTable(schema, table());
    }

    public int rowsAffected() {
        return 0;
    }

}
