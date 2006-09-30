package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.util.ValueObject;

/** 
 * @internal
 * A raw table reference out of the parser (that is, we
 * have not yet applied the default schema nor checked
 * that the table exists, nor canonicalized the table name).
 */
public class UnresolvedTableReference extends ValueObject {

    private final String tableName;
    private final String schema;

    public UnresolvedTableReference(String tableName) {
        this(null, tableName);
        if (tableName == null) {
            throw new NullPointerException();
        }
    }

    public UnresolvedTableReference(String schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
    }

    /**
     * @internal
     * Generally callers will want to resolve the table first, and then
     * consult the resolved table.  So this method might be a bit of a special case.
     * (One issue: we might want the original capitalization
     * for error messages, but either (a) we could have TableReference
     * remember both original and canonical, or (b) canonical is
     * probably OK for messages; it is just forcing everything to
     * upper or lower case which we consider kind of unfriendly).
     */
    public String tableName() {
        return tableName;
    }

    public String schema(String defaultSchema) {
        return schema == null ? defaultSchema : schema;
    }
    
    public TableReference resolve(DataStore store, String defaultSchema,
        String additionalTable) {
        if (defaultSchema == null) {
            throw new NullPointerException("Default schema shouldn't be null");
        }
        
        String schemaToUse = schema(defaultSchema);

        if (tableName.equalsIgnoreCase(additionalTable)) {
            return new TableReference(schemaToUse, additionalTable);
        }

        String canonicalTableName = 
            store.schema(schemaToUse).lookUpTable(tableName);
        return new TableReference(schemaToUse, canonicalTableName);
    }

}
