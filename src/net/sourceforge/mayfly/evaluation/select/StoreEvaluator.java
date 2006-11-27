package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;

public class StoreEvaluator extends Evaluator {

    private final DataStore store;
    private final String currentSchema;

    public StoreEvaluator(DataStore store, String currentSchema) {
        this.store = store;
        this.currentSchema = currentSchema;
        if (store == null) {
            throw new NullPointerException("no store");
        }
        if (currentSchema == null) {
            throw new NullPointerException("no current schema");
        }
    }

    public StoreEvaluator(Schema schema) {
        this(new DataStore(schema), DataStore.ANONYMOUS_SCHEMA_NAME);
    }

    public DataStore store() {
        return store;
    }

    public String currentSchema() {
        return currentSchema;
    }

}
