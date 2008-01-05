package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class DropConstraint extends Command {

    private final UnresolvedTableReference table;
    private final String constraintName;

    public DropConstraint(UnresolvedTableReference table, String constraintName) {
        this.table = table;
        this.constraintName = constraintName;
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        TableReference reference = table.resolve(store, currentSchema, null);
        return new UpdateStore(
            store.dropConstraint(reference, constraintName), 
            0);
    }

}
