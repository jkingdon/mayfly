package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class ModifyColumn extends Command {

    private final UnresolvedTableReference table;
    private final Column newColumn;

    public ModifyColumn(UnresolvedTableReference table, Column newColumn) {
        this.table = table;
        this.newColumn = newColumn;
    }

    @Override
    public UpdateStore update(DataStore store, String defaultSchema) {
        TableReference reference = table.resolve(store, defaultSchema, null);
        
        return store.modifyColumn(reference, newColumn);
    }

}
