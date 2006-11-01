package net.sourceforge.mayfly.datastore.constraint;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.parser.Location;

public abstract class Constraint {

    private final String constraintName;

    public Constraint(String constraintName) {
        this.constraintName = constraintName;
    }

    public void checkDuplicates(List constraintsToCheckAgainst) {
        if (hasConstraint(constraintName, constraintsToCheckAgainst)) {
            /* Are names supposed to be unique per-table?  per-schema?
               Where should we check this (someplace that has location
               available)?  */
            throw new MayflyException(
                "duplicate constraint name " + constraintName);
        }
    }

    private boolean hasConstraint(String constraintName, List constraints) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint key = (Constraint) iter.next();
            if (key.nameMatches(constraintName)) {
                return true;
            }
        }
        return false;
    }

    public boolean nameMatches(String target) {
        if (constraintName == null) {
            return false;
        }
        return this.constraintName.equalsIgnoreCase(target);
    }

    abstract public void checkExistingRows(
        DataStore store, TableReference table);

    abstract public void check(
        Rows existingRows, Row proposedRow, Location location);

    public void checkInsert(DataStore store, String schema, String table, 
        Row proposedRow, Location location) {
        // overridden for foreign key only, not others.
    }

    public DataStore checkDelete(DataStore store, String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        // overridden for foreign key only, not others.
        return store;
    }

    /**
     * @internal
     * @return Should we keep this column?
     */
    abstract public boolean checkDropColumn(TableReference table, String column);

    public void checkDropTargetColumn(TableReference table, String column) {
        // only does something for foreign key.
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        /* Only does something for foreign key.  That sounds right
           (foreign keys are the only ones which involve 2 tables).
         */        
    }

    public boolean canBeTargetOfForeignKey(String targetColumn) {
        return false;
    }

}
