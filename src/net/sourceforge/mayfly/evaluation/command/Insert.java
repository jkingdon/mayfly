package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;

import java.util.List;

public class Insert extends Command {

    private final InsertTable table;
    private final List columns;
    private final List values;

    /** @param values List of values to insert, where each element is cell contents
     * or null for the default value
     */
    public Insert(InsertTable table, List columns, List values) {
        this.table = table;
        this.columns = columns;
        this.values = values;
    }

    public String table() {
        return table.tableName();
    }

    public DataStore update(DataStore store, String currentSchema) {
        if (columns == null) {
            return store.addRow(schemaToUse(currentSchema), table(), values);
        } else {
            return store.addRow(schemaToUse(currentSchema), table(), columns, values);
        }
    }

    private String schemaToUse(String currentSchema) {
        return table.schema(currentSchema);
    }

    public int rowsAffected() {
        return 1;
    }
    
}
