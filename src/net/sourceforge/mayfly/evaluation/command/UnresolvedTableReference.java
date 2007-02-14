package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.parser.Location;

/** 
 * @internal
 * A raw table reference out of the parser (that is, we
 * have not yet applied the default schema nor checked
 * that the table exists, nor canonicalized the table name).
 */
public class UnresolvedTableReference {

    private final String tableName;
    private final String schema;
    public final Location location;
    public final Options options;

    public UnresolvedTableReference(String tableName) {
        this(tableName, Location.UNKNOWN);
    }

    public UnresolvedTableReference(String tableName, Location location) {
        this(null, tableName, location);
        if (tableName == null) {
            throw new NullPointerException();
        }
    }

    public UnresolvedTableReference(
        String schema, String tableName, Location location) {
        this(schema, tableName, location, new Options());
    }

    public UnresolvedTableReference(String schema, String tableName, 
        Location location, Options options) {
        this.schema = schema;
        this.tableName = tableName;
        this.location = location;
        this.options = options;
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

        if (options.tableNamesEqual(tableName, additionalTable)) {
            return new TableReference(schemaToUse, additionalTable);
        }

        String canonicalTableName = 
            store.schema(schemaToUse).lookUpTable(tableName, location, options);
        return new TableReference(schemaToUse, canonicalTableName);
    }

}
