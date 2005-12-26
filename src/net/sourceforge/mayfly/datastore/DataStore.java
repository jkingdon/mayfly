package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.*;

import java.util.*;

/**
 * A data store is an immutable object containing data
 * and metadata for a set of tables.
 * 
 * <p>You typically get one via {@link net.sourceforge.mayfly.Database#dataStore()}
 * and then pass it to {@link net.sourceforge.mayfly.Database#Database(DataStore)}.</p>
 * 
 * @internal
 * As with the rest of our immutable objects, all fields should
 * be final and have types which are immutable (like String,
 * Double, ImmutableList, long, etc).
 */
public class DataStore {

    public static final String ANONYMOUS_SCHEMA = new String();

    private final ImmutableMap namedSchemas;
    
    public DataStore() {
        this(new Schema());
    }

    public DataStore(Schema anonymousSchema) {
        this(new ImmutableMap().with(ANONYMOUS_SCHEMA, anonymousSchema));
    }

    public DataStore(ImmutableMap namedSchemas) {
        this.namedSchemas = namedSchemas;
    }

    public DataStore addSchema(String newSchemaName, Schema newSchema) {
        if (schemaExists(newSchemaName)) {
            throw new MayflyException("schema " + newSchemaName + " already exists");
        }
        return new DataStore(namedSchemas.with(newSchemaName, newSchema));
    }

    public DataStore replace(String newSchemaName, Schema newSchema) {
        if (schemaExists(newSchemaName)) {
            return new DataStore(namedSchemas.with(newSchemaName, newSchema));
        } else {
            throw new MayflyInternalException("no schema " + newSchemaName);
        }
    }

    private boolean schemaExists(String newSchemaName) {
        // FIXME: should be case insensitive
        return namedSchemas.containsKey(newSchemaName);
    }

    public Schema schema(String schema) {
        if (schemaExists(schema)) {
            return (Schema) namedSchemas.get(schema);
        }
        throw new MayflyException("no schema " + schema);
    }

    public Schema anonymousSchema() {
        return (Schema) namedSchemas.get(ANONYMOUS_SCHEMA);
    }
    
    public DataStore dropTable(String schema, String table) {
        return replace(schema, schema(schema).dropTable(table));
    }

    public TableData table(String schema, String table) {
        return schema(schema).table(table);
    }

    public TableData table(String table) {
        return anonymousSchema().table(table);
    }

    public Set tables(String schema) {
        return schema(schema).tables();
    }

    public DataStore addRow(String schema, String table, List columnNames, List values) {
        return replace(schema, schema(schema).addRow(table, columnNames, values));
    }

    public DataStore addRow(String schema, String table, List values) {
        return replace(schema, schema(schema).addRow(table, values));
    }

}
