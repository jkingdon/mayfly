package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.parser.Location;

/**
   A reference to a table which has not only been validated against the
   tables which really exist, but also potentially resides in the
   {@link DataStore} itself.  This means that it should not contain
   a {@link Location} (because it is longer-lived than a single command),
   and it should not contain {@link Options} (because we should re-fetch
   options from {@link Database} at the start of every command, to avoid
   situations of consulting a stale options object).
 */
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

    public static String formatTableName(
        String defaultSchema, String schemaToFormat, String tableToFormat) {
        StringBuilder result = new StringBuilder();
        if (!schemaToFormat.equalsIgnoreCase(defaultSchema)) {
            result.append(schemaToFormat);
            result.append(".");
        }
        result.append(tableToFormat);
        return result.toString();
    }
    
    public String toString() {
        return formatTableName(null, schema, table);
    }
    
    public String displayName(String defaultSchema) {
        return formatTableName(defaultSchema, schema, table);
    }

}
