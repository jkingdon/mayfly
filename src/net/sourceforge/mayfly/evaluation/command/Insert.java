package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.util.ImmutableList;

public class Insert extends Command {

    private final InsertTable table;
    private final ImmutableList columnNames;
    private final ImmutableList values;

    /** @param values List of values to insert, where each element is cell contents
     * or null for the default value
     */
    public Insert(InsertTable table, ImmutableList columnNames, ImmutableList values) {
        this.table = table;
        this.columnNames = columnNames;
        this.values = values;
    }

    public String table() {
        return table.tableName();
    }

    public UpdateStore update(DataStore store, String currentSchema) {
        return new UpdateStore(insertOneRow(store, currentSchema), 1);
    }

    private DataStore insertOneRow(DataStore store, String currentSchema) {
        if (columnNames == null) {
            return store.addRow(schemaToUse(currentSchema), table(), values);
        } else {
            return store.addRow(schemaToUse(currentSchema), table(), columnNames, values);
        }
    }

    private String schemaToUse(String currentSchema) {
        return table.schema(currentSchema);
    }

}
