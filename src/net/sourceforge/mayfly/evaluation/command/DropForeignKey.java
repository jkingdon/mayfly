package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class DropForeignKey extends Command {

    private final String constraintName;
    private final UnresolvedTableReference table;

    public DropForeignKey(UnresolvedTableReference table, String constraintName) {
        this.table = table;
        this.constraintName = constraintName;
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        TableReference reference = table.resolve(store, currentSchema, null);
        return new UpdateStore(
            store.dropForeignKey(reference, constraintName), 
            0);
    }

}
