package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Index;
import net.sourceforge.mayfly.datastore.TableReference;

public class CreateIndex extends Command {

    private final UnresolvedTableReference table;
    private final String indexName;
    private final ColumnNames columns;
    private final boolean unique;

    public CreateIndex(UnresolvedTableReference table, 
        String indexName, ColumnNames columns, boolean unique) {
        this.table = table;
        this.indexName = indexName;
        this.columns = columns;
        this.unique = unique;
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        DataStore withConstraint = addUniqueConstraint(store, currentSchema);
        DataStore newStore = addIndex(withConstraint, currentSchema);
        return new UpdateStore(newStore, 0);
    }

    private DataStore addIndex(DataStore store, String currentSchema) {
        Index index = new Index(indexName, columns);
        TableReference reference = table.resolve(store, currentSchema, null);
        return store.addIndex(reference, index);
    }

    private DataStore addUniqueConstraint(
        DataStore store, String currentSchema) {
        if (unique) {
            UnresolvedUniqueConstraint constraint = 
                new UnresolvedUniqueConstraint(columns, null);
            return new AddConstraint(table, constraint)
                .update(store, currentSchema)
                .store();
        }
        else {
            return store;
        }
    }

}
