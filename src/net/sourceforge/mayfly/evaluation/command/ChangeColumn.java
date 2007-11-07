package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class ChangeColumn extends Command {

    private final UnresolvedTableReference table;
    private final String oldName;
    private final Column newColumn;

    public ChangeColumn(UnresolvedTableReference table, 
        String oldName, Column newColumn) {
            this.table = table;
            this.oldName = oldName;
            this.newColumn = newColumn;
    }

    @Override
    public UpdateStore update(DataStore store, String defaultSchema) {
        TableReference reference = table.resolve(store, defaultSchema, null);
        
        DataStore afterRename = store.renameColumn(
            reference, oldName, newColumn.columnName());
        return afterRename.modifyColumn(reference, newColumn);
    }

}
