package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.command.UnresolvedTableReference;
import net.sourceforge.mayfly.evaluation.from.FromTable;

public class StoreEvaluator extends Evaluator {

    private final DataStore store;
    private final String currentSchema;
    private Options options;

    public StoreEvaluator(DataStore store, String currentSchema) {
        this(store, currentSchema, new Options());
    }

    public StoreEvaluator(DataStore store, String currentSchema, Options options) {
        this.store = store;
        this.currentSchema = currentSchema;
        if (store == null) {
            throw new NullPointerException("no store");
        }
        if (currentSchema == null) {
            throw new NullPointerException("no current schema");
        }
        
        this.options = options;
    }

    public StoreEvaluator(Schema schema) {
        this(new DataStore(schema), DataStore.ANONYMOUS_SCHEMA_NAME);
    }

    @Override
    public DataStore store() {
        return store;
    }

    @Override
    public String currentSchema() {
        return currentSchema;
    }
    
    @Override
    public TableData table(FromTable table) {
        UnresolvedTableReference unresolved = new UnresolvedTableReference(
            currentSchema, table.tableName, table.location, options);
        TableReference resolved = unresolved.resolve(store, currentSchema, null);
        return store().table(resolved);
    }
    
    @Override
    public Options options() {
        return options;
    }

}
