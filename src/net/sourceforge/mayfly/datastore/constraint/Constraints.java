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

    public final ImmutableList constraints;

    public Constraints(PrimaryKey primaryKey, ImmutableList uniqueConstraints,
        ImmutableList foreignKeys) {
        this(append(primaryKey, uniqueConstraints, foreignKeys));
    }

    private static ImmutableList append(PrimaryKey primaryKey, 
        ImmutableList constraints, ImmutableList foreignKeys) {
        ImmutableList all = constraints.withAll(foreignKeys);
        if (primaryKey != null) {
            all = all.with(primaryKey);
        }
        return all;
    }

    public Constraints(ImmutableList all) {
        this.constraints = all;
        checkDuplicates(all);
    }

    private void checkDuplicates(ImmutableList constraints) {
        for (int i = 0; i < constraints.size(); ++i) {
            Constraint constraint = (Constraint) constraints.get(i);
            constraint.checkDuplicates(constraints.subList(0, i));
        }
    }

    public Constraints() {
        this(new ImmutableList());
    }

    /**
     * @internal
     * Check some constraints.
     * 
     * Not-null is checked in 
     * {@link Column#coerce(net.sourceforge.mayfly.datastore.Cell, Location)}
     */
    public void check(Rows rows, Row newRow, Location location) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            constraint.check(rows, newRow, location);
        }
    }

    public void checkInsert(
        DataStore store, String schema, String table, Row proposedRow, Location location) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            constraint.checkInsert(store, schema, table, proposedRow, location);
        }
    }

    public DataStore checkDelete(
        DataStore store, String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            store = constraint.checkDelete(store, schema, table, 
                rowToDelete, replacementRow);
        }
        return store;
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            constraint.checkDropTable(store, schema, table);
        }
    }

    public Constraints dropColumn(TableReference table, String column) {
        return new Constraints(
            filterConstraintsForDropColumn(table, column)
        );
    }

    private ImmutableList filterConstraintsForDropColumn(
        TableReference table, String column) {
        List newConstraints = new ArrayList();
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            if (constraint.checkDropColumn(table, column)) {
                newConstraints.add(constraint);
            }
        }
        return new ImmutableList(newConstraints);
    }

    public void checkDropColumn(TableReference table, String column) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            constraint.checkDropTargetColumn(table, column);
        }
    }

    public Constraints dropForeignKey(String constraintName) {
        return new Constraints(
            constraintsWithout(constraintName, ForeignKey.class));
    }

    private ImmutableList constraintsWithout(String constraintName, Class constraintType) {
        Constraint found = null;
        List keys = new ArrayList();
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint key = (Constraint) iter.next();
            if (!key.nameMatches(constraintName)) {
                keys.add(key);
            }
            else {
                found = key;
            }
        }
        if (found == null) {
            throw new MayflyException("no constraint " + constraintName);
        }
        if (found.getClass() != constraintType) {
            throw new MayflyException(
                "constraint " + constraintName + 
                " is not a " + describe(constraintType));
        }
        return new ImmutableList(keys);
    }

    private String describe(Class constraintType) {
        if (constraintType == ForeignKey.class) {
            return "foreign key";
        }
        else {
            return constraintType.getName();
        }
    }

    public Constraints addConstraint(Constraint constraint) {
        constraint.checkDuplicates(constraints);
        return new Constraints(constraints.with(constraint));
    }

    public boolean hasPrimaryKeyOrUnique(String targetColumn) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            if (constraint.matchesPrimaryKeyOrUnique(targetColumn)) {
                return true;
            }
        }
        return false;
    }

}
