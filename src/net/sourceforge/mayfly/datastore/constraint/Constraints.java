package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Iterator;

public class Constraints {

    private final PrimaryKey primaryKey;
    private final ImmutableList constraints;
    private final ImmutableList foreignKeyConstraints;

    public Constraints(PrimaryKey primaryKey, ImmutableList constraints,
        ImmutableList foreignKeyConstraints) {
        this.primaryKey = primaryKey;
        this.constraints = constraints;
        this.foreignKeyConstraints = foreignKeyConstraints;
    }

    public Constraints() {
        this(null, new ImmutableList(), new ImmutableList());
    }

    public void check(Rows rows, Row newRow, Location location) {
        if (primaryKey != null) {
            primaryKey.check(rows, newRow, location);
        }
        
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            constraint.check(rows, newRow, location);
        }
    }

    public void checkInsert(
        DataStore store, String schema, String table, Row proposedRow, Location location) {
        for (Iterator iter = foreignKeyConstraints.iterator(); iter.hasNext();) {
            ForeignKey constraint = (ForeignKey) iter.next();
            constraint.checkInsert(store, schema, table, proposedRow, location);
        }
    }

    public DataStore checkDelete(
        DataStore store, String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        for (Iterator iter = foreignKeyConstraints.iterator(); iter.hasNext();) {
            ForeignKey constraint = (ForeignKey) iter.next();
            store = constraint.checkDelete(store, schema, table, 
                rowToDelete, replacementRow);
        }
        return store;
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        for (Iterator iter = foreignKeyConstraints.iterator(); iter.hasNext();) {
            ForeignKey constraint = (ForeignKey) iter.next();
            constraint.checkDropTable(store, schema, table);
        }
    }

}
