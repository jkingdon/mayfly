package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.StoreEvaluator;
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
public class RealChecker extends Checker {

    private final String schema;
    private final String table;
    private DataStore store;
    private final Location location;
    private Cell newIdentityValue = null;
    private final Options options;

    /**
     * @internal
     * @param evaluationSchema The schema which is being used
     * in evaluation (constraints may refer to others).
     * @param evaluationTable The table we are actually
     * deleting a row from (or updating a row in) (contraints
     * may refer to others).
     */
    public RealChecker(DataStore store, TableReference table) {
        this(store, table, Location.UNKNOWN, new Options());
    }

    public RealChecker(DataStore store, TableReference table, 
        Location location, Options options) {
        this(store, table.schema(), table.tableName(), location, options);
    }

    public RealChecker(DataStore store, String evaluationSchema, String evaluationTable, 
        Location location, Options options) {
        this.store = store;
        this.schema = evaluationSchema;
        this.table = evaluationTable;
        this.location = location;
        this.options = options;
    }

    @Override
    public void checkDelete(Row rowToDelete, Row replacementRow) {
        store = store.checkDelete(schema, table, rowToDelete, replacementRow);
    }

    @Override
    public void checkInsert(Constraints constraints, Row proposedRow) {
        constraints.checkInsert(store, schema, table, proposedRow, location);
    }

    @Override
    public void checkDropTable() {
        store.checkDropTable(schema, table);
    }

    @Override
    public DataStore store() {
        return store;
    }

    @Override
    public String schema() {
        return schema;
    }

    @Override
    public void setIdentityValue(Cell cell) {
        this.newIdentityValue = cell;
    }

    @Override
    public Cell newIdentityValue() {
        return newIdentityValue;
    }
    
    @Override
    public boolean evaluate(Condition condition, Row row, String tableName) {
        return condition.evaluate(new ResultRow(row, tableName, options), 
            new StoreEvaluator(store, schema, options));
    }
    
}
