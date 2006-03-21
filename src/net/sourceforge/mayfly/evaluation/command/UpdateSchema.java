package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.Schema;

public class UpdateSchema {

    private final Schema schema;
    private final int rowsAffected;

    public UpdateSchema(Schema schema, int rowsAffected) {
        this.schema = schema;
        this.rowsAffected = rowsAffected;
    }

    public Schema schema() {
        return schema;
    }

    public int rowsAffected() {
        return rowsAffected;
    }

}
