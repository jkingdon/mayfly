package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class RenameTable extends Command {

    private final UnresolvedTableReference existingTable;
    private final String newName;

    public RenameTable(UnresolvedTableReference existingTable, String newName) {
        this.existingTable = existingTable;
        this.newName = newName;
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        TableReference reference = existingTable.resolve(store, currentSchema, null);
        return store.renameTable(reference, newName);
    }

}
