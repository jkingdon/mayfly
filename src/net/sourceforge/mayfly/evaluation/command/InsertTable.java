package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.util.ValueObject;

// TODO: probably should be called TableReference or some such.
// Might be an open question about when a user-specified name
// (might differ in case from canonical, might not exist) gets
// resolved.
// (also when the default schema gets applied)
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

    public InsertTable fillInSchema(String defaultSchema) {
        if (defaultSchema == null) {
            throw new NullPointerException("Default schema shouldn't be null");
        }

        if (schema == null) {
            return new InsertTable(defaultSchema, tableName);
        }
        else {
            return this;
        }
    }

}
