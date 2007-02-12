package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.constraint.Constraint;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.RealChecker;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.command.UnresolvedTableReference;
import net.sourceforge.mayfly.evaluation.command.UpdateSchema;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableMap;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

    public static final String ANONYMOUS_SCHEMA_NAME = "";
    public static final CaseInsensitiveString ANONYMOUS_SCHEMA = 
        new CaseInsensitiveString(ANONYMOUS_SCHEMA_NAME);

    private final ImmutableMap schemas;
    
    public DataStore() {
        this(new Schema());
    }

    public DataStore(Schema anonymousSchema) {
        this(new ImmutableMap().with(ANONYMOUS_SCHEMA, anonymousSchema));
    }

    private DataStore(ImmutableMap namedSchemas) {
        this.schemas = namedSchemas;
    }

    public DataStore addSchema(String newSchemaName, Schema newSchema) {
        if (schemaExists(newSchemaName)) {
            throw new MayflyException("schema " + newSchemaName + " already exists");
        }
        ImmutableMap newSchemas = schemas.with(new CaseInsensitiveString(newSchemaName), newSchema);
        return new DataStore(newSchemas);
    }

    public DataStore replace(String newSchemaName, Schema newSchema) {
        return replace(schemas, newSchemaName, newSchema);
    }

    private DataStore replace(ImmutableMap existingSchemas, 
        String newSchemaName, Schema newSchema) {
        if (schemaExists(newSchemaName)) {
            ImmutableMap newSchemas = existingSchemas.with(
                new CaseInsensitiveString(newSchemaName), newSchema);
            return new DataStore(newSchemas);
        } 
        else {
            throw new MayflyInternalException("no schema " + newSchemaName);
        }
    }

    private boolean schemaExists(String newSchemaName) {
        return schemas.containsKey(new CaseInsensitiveString(newSchemaName));
    }

    public Schema schema(String schema) {
        if (schemaExists(schema)) {
            return (Schema) schemas.get(new CaseInsensitiveString(schema));
        }
        throw new MayflyException("no schema " + schema);
    }

    public Schema anonymousSchema() {
        return (Schema) schemas.get(ANONYMOUS_SCHEMA);
    }
    
    public DataStore dropTable(TableReference table) {
        Checker checker = new RealChecker(this, table);
        return replace(table.schema(), 
            schema(table.schema()).dropTable(checker, table.tableName()));
    }

    public TableData table(String schema, String table) {
        return schema(schema).table(table);
    }

    public TableData table(TableReference table) {
        return schema(table.schema()).table(table.tableName());
    }

    public TableData table(String table) {
        return anonymousSchema().table(table);
    }

    public Set tables(String schema) {
        return schema(schema).tables();
    }

    public DataStore addRow(String schema, String table, 
        List columnNames, ValueList values, Checker checker) {
        return replace(schema,
            schema(schema).addRow(checker, table, columnNames, values));
    }

    public DataStore addRow(String schema, String table, ValueList values, Checker checker) {
        return replace(schema, schema(schema).addRow(checker, table, values));
    }

    public Set schemas() {
        Set names = new TreeSet();
        for (Iterator iter = schemas.keySet().iterator(); iter.hasNext();) {
            CaseInsensitiveString key = (CaseInsensitiveString) iter.next();
            if (!key.equals(ANONYMOUS_SCHEMA)) {
                names.add(key.getString());
            }
        }
        return names;
    }

    public UpdateStore update(String schema, String table, 
        List setClauses, Condition where, Options options) {
        Checker checker = new RealChecker(this, schema, table, 
            Location.UNKNOWN, options);
        UpdateSchema result = 
            schema(schema).update(checker, table, setClauses, where);
        return replaceSchema(schema, result);
    }

    public UpdateStore delete(String schema, String table, Condition where,
        Options options) {
        Checker checker = new RealChecker(this, schema, table, 
            Location.UNKNOWN, options);
        UpdateSchema result = schema(schema).delete(table, where, checker);
        
        /**
         * Here we merge the schemas: the one corresponding to schema
         * was returned
         * by delete, and the rest come in via the checker.
         * This way the checker is the only thing which operates across
         * schemas - the regular code just affects the one.
         */
        ImmutableMap schemas = checker.store().schemas;
        DataStore newStore = replace(schemas, schema, result.schema());

        return new UpdateStore(
            newStore, 
            result.rowsAffected()
        );
    }

    private UpdateStore replaceSchema(String schema, UpdateSchema result) {
        return new UpdateStore(
            replace(schema, result.schema()), 
            result.rowsAffected()
        );
    }

    public DataStore checkDelete(String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        DataStore store = this;
        for (Iterator iter = schemas.values().iterator(); iter.hasNext();) {
            Schema potentialReferencer = (Schema) iter.next();
            store = potentialReferencer.checkDelete(
                store,
                schema, table, rowToDelete, replacementRow);
        }
        return store;
    }

    public void checkDropTable(String schema, String table) {
        for (Iterator iter = schemas.values().iterator(); iter.hasNext();) {
            Schema potentialReferencer = (Schema) iter.next();
            potentialReferencer.checkDropTable(this, schema, table);
        }
    }

    public UpdateStore dropColumn(TableReference table, String column) {
        for (Iterator iter = schemas.values().iterator(); iter.hasNext();) {
            Schema potentialReferencer = (Schema) iter.next();
            potentialReferencer.checkDropColumn(table, column);
        }

        Schema existing = schema(table.schema());
        Schema updatedSchema = existing.dropColumn(table, column);
        return new UpdateStore(replace(table.schema(), updatedSchema), 0);
    }

    public UpdateStore modifyColumn(TableReference table, Column newColumn) {
        Schema existing = schema(table.schema());
        Schema updatedSchema = existing.modifyColumn(table.tableName(), newColumn);
        return new UpdateStore(replace(table.schema(), updatedSchema), 0);
    }

    public DataStore dropForeignKey(TableReference table, String constraintName) {
        Schema existing = schema(table.schema());
        return replace(
            table.schema(), 
            existing.dropForeignKey(table.tableName(), constraintName)
        );
    }

    public DataStore addConstraint(TableReference table, Constraint key) {
        Schema existing = schema(table.schema());
        return replace(
            table.schema(), 
            existing.addConstraint(table.tableName(), key)
        );
    }

    public boolean hasTable(
        UnresolvedTableReference table, String defaultSchema) {
        Schema schema = schema(table.schema(defaultSchema));
        return schema.hasTable(table.tableName());
    }

}
