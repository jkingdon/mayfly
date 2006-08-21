package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.constraint.Constraints;

/**
 * This class knows about the various things which are
 * needed to check datastore-wide constraints.
 * Passing around those things as an object seems
 * cleaner than having them exposed in various
 * pieces of code which really should just be
 * handling a single table or schema.
 * 
 * We also keep track of changes made because of
 * ON DELETE CASCADE and similar.
 */
public class Checker {

    private final String schema;
    private final String table;
    private DataStore store;

    /**
     * @internal
     * @param evaluationSchema The schema which is being used
     * in evaluation (constraints may refer to others).
     * @param evaluationTable The table we are actually
     * deleting a row from (or updating a row in) (contraints
     * may refer to others).
     */
    public Checker(DataStore store, String evaluationSchema, 
        String evaluationTable) {
        this.store = store;
        this.schema = evaluationSchema;
        this.table = evaluationTable;
    }

    public void checkDelete(Row rowToDelete, Row replacementRow) {
        store = store.checkDelete(schema, table, rowToDelete, replacementRow);
    }

    public void checkInsert(Constraints constraints, Row proposedRow) {
        constraints.checkInsert(store, schema, table, proposedRow);
    }

    public void checkDropTable() {
        store.checkDropTable(schema, table);
    }

    public DataStore store() {
        return store;
    }

    public String schema() {
        return schema;
    }
    
}
