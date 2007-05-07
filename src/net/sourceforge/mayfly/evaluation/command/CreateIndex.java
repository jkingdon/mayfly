package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;

import java.util.List;

public class CreateIndex extends Command {

    private final UnresolvedTableReference table;
    private final UnresolvedUniqueConstraint constraint;
    private final boolean unique;

    public CreateIndex(UnresolvedTableReference table, 
        List columns, boolean unique) {
        this.table = table;
        this.constraint = new UnresolvedUniqueConstraint(columns, null);
        this.unique = unique;
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        if (unique) {
            return new AddConstraint(table, constraint)
                .update(store, currentSchema);
        }
        else {
            return new UpdateStore(store, 0);
        }
    }

}
