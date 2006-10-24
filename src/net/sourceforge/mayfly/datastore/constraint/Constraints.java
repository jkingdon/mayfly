package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Constraints {

    public final PrimaryKey primaryKey;
    public final ImmutableList constraints;
    public final ImmutableList foreignKeys;

    public Constraints(PrimaryKey primaryKey, ImmutableList constraints,
        ImmutableList foreignKeys) {
        this.primaryKey = primaryKey;
        this.constraints = constraints;
        this.foreignKeys = foreignKeys;
    }

    public Constraints() {
        this(null, new ImmutableList(), new ImmutableList());
    }

    /**
     * @internal
     * Check some constraints.
     * 
     * Not-null is checked in 
     * {@link Column#coerce(net.sourceforge.mayfly.datastore.Cell, Location)}
     */
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
        for (Iterator iter = foreignKeys.iterator(); iter.hasNext();) {
            ForeignKey constraint = (ForeignKey) iter.next();
            constraint.checkInsert(store, schema, table, proposedRow, location);
        }
    }

    public DataStore checkDelete(
        DataStore store, String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        for (Iterator iter = foreignKeys.iterator(); iter.hasNext();) {
            ForeignKey constraint = (ForeignKey) iter.next();
            store = constraint.checkDelete(store, schema, table, 
                rowToDelete, replacementRow);
        }
        return store;
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        for (Iterator iter = foreignKeys.iterator(); iter.hasNext();) {
            ForeignKey constraint = (ForeignKey) iter.next();
            constraint.checkDropTable(store, schema, table);
        }
    }

    public Constraints dropColumn(TableReference table, String column) {
        return new Constraints(
            filterPrimaryKeyForDropColumn(column), 
            filterConstraintsForDropColumn(column), 
            filterForeignKeysForDropColumn(table, column));
    }

    private PrimaryKey filterPrimaryKeyForDropColumn(String column) {
        PrimaryKey key = primaryKey;
        if (primaryKey != null) {
            boolean keep = primaryKey.checkDropColumn(column);
            if (!keep) {
                key = null;
            }
        }
        return key;
    }

    private ImmutableList filterConstraintsForDropColumn(String column) {
        List newConstraints = new ArrayList();
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            if (constraint.checkDropColumn(column)) {
                newConstraints.add(constraint);
            }
        }
        return new ImmutableList(newConstraints);
    }

    private ImmutableList filterForeignKeysForDropColumn(
        TableReference table, String column) {
        List keys = new ArrayList();
        for (Iterator iter = foreignKeys.iterator(); iter.hasNext();) {
            ForeignKey key = (ForeignKey) iter.next();
            if (key.checkDropReferencerColumn(table, column)) {
                keys.add(key);
            }
        }
        return new ImmutableList(keys);
    }

    public void checkDropColumn(TableReference table, String column) {
        for (Iterator iter = foreignKeys.iterator(); iter.hasNext();) {
            ForeignKey key = (ForeignKey) iter.next();
            key.checkDropTargetColumn(table, column);
        }
    }

    public Constraints dropForeignKey(String constraintName) {
        return new Constraints(
            primaryKey, 
            constraints, 
            foreignKeysWithout(constraintName));
    }

    private ImmutableList foreignKeysWithout(String constraintName) {
        boolean found = false;
        List keys = new ArrayList();
        for (Iterator iter = foreignKeys.iterator(); iter.hasNext();) {
            ForeignKey key = (ForeignKey) iter.next();
            if (!key.nameMatches(constraintName)) {
                keys.add(key);
            }
            else {
                found = true;
            }
        }
        if (!found) {
            throw new MayflyException("no foreign key " + constraintName);
        }
        return new ImmutableList(keys);
    }

    public Constraints addForeignKey(ForeignKey key) {
        return new Constraints(
            primaryKey, 
            constraints, 
            foreignKeys.with(key));
    }

}
