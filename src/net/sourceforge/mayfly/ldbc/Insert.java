package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;

import java.util.*;

public class Insert extends Command {

    private final InsertTable table;
    private final List columns;
    private final List values;

    public Insert(InsertTable table, List columns, List values) {
        this.table = table;
        this.columns = columns;
        this.values = values;
    }

    public String table() {
        return table.tableName();
    }

    public void substitute(Collection jdbcParameters) {
        substitute(jdbcParameters.iterator());
    }

    private void substitute(Iterator jdbcParameters) {
        for (int i = 0; i < values.size(); ++i) {
            if (values.get(i) instanceof JdbcParameter) {
                values.set(i, jdbcParameters.next());
            }
        }
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
    
    public int parameterCount() {
        int count = 0;
        for (int i = 0; i < values.size(); ++i) {
            if (values.get(i) instanceof JdbcParameter) {
                ++count;
            }
        }
        return count;
    }

}
