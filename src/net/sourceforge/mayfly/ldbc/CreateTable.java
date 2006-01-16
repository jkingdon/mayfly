package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;

import java.util.*;

public class CreateTable extends Command {

    private String table;
    private List columnNames;

    public CreateTable(String table, List columnNames) {
        this.table = table;
        this.columnNames = columnNames;
    }

    public String table() {
        return table;
    }

    public List columnNames() {
        return columnNames;
    }

    public void substitute(Collection jdbcParameters) {
    }

    public DataStore update(DataStore store, String schema) {
        Schema oldSchema = store.schema(schema);
        Schema updatedSchema = update(oldSchema);
        return store.replace(schema, updatedSchema);
    }

    public Schema update(Schema anonymousSchema) {
        return anonymousSchema.createTable(table(), columnNames());
    }

    public int rowsAffected() {
        return 0;
    }

    public int parameterCount() {
        return 0;
    }

}
