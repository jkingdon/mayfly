package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.parser.Location;

/**
 * @internal
 * This class knows about the various things which are
 * needed to check datastore-wide constraints.
 * Passing around those things as an object seems
 * cleaner than having them exposed in various
 * pieces of code which really should just be
 * handling a single table or schema.
 * 
 * We also keep track of changes made because of
 * ON DELETE CASCADE and similar.
 * 
 * We also keep the lastIdentity, which is something
 * which is computed down on a column level, but needs
 * to get passed up to the store somehow.
 */
public class Checker {

    private final String schema;
    private final String table;
    private DataStore store;
    private final Location location;
    private Cell newIdentityValue = null;

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
        this(store, evaluationSchema, evaluationTable, Location.UNKNOWN);
    }

    public Checker(DataStore store, String evaluationSchema, String evaluationTable, Location location) {
        this.store = store;
        this.schema = evaluationSchema;
        this.table = evaluationTable;
        this.location = location;
    }

    public void checkDelete(Row rowToDelete, Row replacementRow) {
        store = store.checkDelete(schema, table, rowToDelete, replacementRow);
    }

    public void checkInsert(Constraints constraints, Row proposedRow) {
        constraints.checkInsert(store, schema, table, proposedRow, location);
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

    public void setIdentityValue(Cell cell) {
        this.newIdentityValue = cell;
    }

    public Cell lastIdentity(Cell previous) {
        return newIdentityValue != null ? newIdentityValue : previous;
    }
    
}
