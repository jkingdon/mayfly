package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class DropColumn extends Command {

    private final UnresolvedTableReference table;
    private final String column;

    public DropColumn(UnresolvedTableReference table, String column) {
        this.table = table;
        this.column = column;
    }

    @Override
    public UpdateStore update(DataStore store, String defaultSchema) {
        TableReference reference = table.resolve(store, defaultSchema, null);
        
        return store.dropColumn(reference, column);
    }

}
