package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.util.ValueObject;

/** A raw table reference out of the parser (that is, we
 * have not yet applied the default schema nor checked
 * that the table exists, nor canonicalized the table name).
 * 
 * Should probably be renamed to UnresolveTableReference.
 */
public class InsertTable extends ValueObject {

    private final String tableName;
    private final String schema;

    public InsertTable(String tableName) {
        this(null, tableName);
    }

    public InsertTable(String schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
    }

    public String tableName() {
        return tableName;
    }

    public String schema(String defaultSchema) {
        return schema == null ? defaultSchema : schema;
    }
    
    public String schema() {
        assertSchemaIsResolved();
        return schema;
    }

    public void assertSchemaIsResolved() {
        if (schema == null) {
            throw new MayflyInternalException(
                "schema should have already been resolved against the default schema");
        }
    }

    public TableReference resolve(DataStore store, String defaultSchema) {
        if (defaultSchema == null) {
            throw new NullPointerException("Default schema shouldn't be null");
        }

        String schemaToUse = schema == null ? defaultSchema : schema;
        String canonicalTableName = 
            store.schema(schemaToUse).lookUpTable(tableName);
        return new TableReference(schemaToUse, canonicalTableName);
    }

}
