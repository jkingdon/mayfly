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
        this(new ImmutableMap().with(ANONYMOUS_SCHEMA, new Schema()));
    }

    public DataStore(ImmutableMap namedSchemas) {
        this.namedSchemas = namedSchemas;
    }

    public DataStore with(String newSchemaName, Schema newSchema) {
        return new DataStore(namedSchemas.with(newSchemaName, newSchema));
    }

    public DataStore createTable(String table, List columnNames) {
        return with(ANONYMOUS_SCHEMA, anonymousSchema().createTable(table, columnNames));
    }
    
    public Schema schema(String schema) {
        if (namedSchemas.containsKey(schema)) {
            return (Schema) namedSchemas.get(schema);
        }
        throw new MayflyException("no schema " + schema);
    }

    public Schema anonymousSchema() {
        return (Schema) namedSchemas.get(ANONYMOUS_SCHEMA);
    }
    
    public DataStore dropTable(String schema, String table) {
        return with(schema, schema(schema).dropTable(table));
    }

    public TableData table(String schema, String table) {
        return schema(schema).table(table);
    }

    public TableData table(String table) {
        return table(ANONYMOUS_SCHEMA, table);
    }

    public Set tables() {
        return anonymousSchema().tables();
    }

    public DataStore addRow(String schema, String table, List columnNames, List values) {
        return with(schema, schema(schema).addRow(table, columnNames, values));
    }

    public DataStore addRow(String table, List values) {
        return with(ANONYMOUS_SCHEMA, anonymousSchema().addRow(table, values));
    }

    public DataStore addRow(String table, List columnNames, List values) {
        return addRow(ANONYMOUS_SCHEMA, table, columnNames, values);
    }

}
