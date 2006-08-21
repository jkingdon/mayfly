package net.sourceforge.mayfly.datastore;

public class TableReference {

    private final String schema;
    private final String table;

    public TableReference(String schema, String table) {
        if (schema == null) {
            throw new NullPointerException("mayfly internal error");
        }
        if (table == null) {
            throw new NullPointerException("mayfly internal error");
        }
        this.schema = schema;
        this.table = table;
    }

    public String schema() {
        return schema;
    }

    public String tableName() {
        return table;
    }

    public boolean matches(String candidateSchema, String candidateTable) {
        return schema.equalsIgnoreCase(candidateSchema)
            && table.equalsIgnoreCase(candidateTable);
    }

}
