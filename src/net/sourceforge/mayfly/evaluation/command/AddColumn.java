package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Position;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.TableReference;

public class AddColumn extends Command {

    private final UnresolvedTableReference table;
    private final Column newColumn;
    private final Position position;

    public AddColumn(UnresolvedTableReference table, 
        Column newColumn, Position position) {
        this.newColumn = newColumn;
        this.table = table;
        this.position = position;
    }

    public UpdateStore update(DataStore store, String defaultSchema) {
        TableReference reference = table.resolve(store, defaultSchema, null);
        
        Schema existing = store.schema(reference.schema());
        Schema updatedSchema = existing.addColumn(reference.tableName(), 
            newColumn, position);
        return new UpdateStore(store.replace(reference.schema(), updatedSchema), 0);
    }

}
