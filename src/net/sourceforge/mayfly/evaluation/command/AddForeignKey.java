package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.datastore.constraint.ForeignKey;

public class AddForeignKey extends Command {

    private final UnresolvedForeignKey key;
    private final UnresolvedTableReference table;

    public AddForeignKey(UnresolvedTableReference table, UnresolvedForeignKey key) {
        this.table = table;
        this.key = key;
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        TableReference reference = table.resolve(store, currentSchema, null);
        ForeignKey resolved = 
            key.resolve(store, reference.schema(), reference.tableName());
        resolved.checkExistingRows(store);
        return new UpdateStore(
            store.addForeignKey(reference, resolved), 
            0);
    }

}
