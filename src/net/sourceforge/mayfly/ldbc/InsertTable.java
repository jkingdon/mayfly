package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.util.*;

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

}
