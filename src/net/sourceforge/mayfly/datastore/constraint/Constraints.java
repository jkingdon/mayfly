package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Constraints {

    private final ImmutableList constraints;

    public Constraints() {
        this(new ImmutableList());
    }

    public Constraints(ImmutableList constraints) {
        this.constraints = constraints;
        checkDuplicates(constraints);
        checkOnlyOnePrimaryKey(constraints);
    }

    private static void checkDuplicates(ImmutableList constraints) {
        for (int i = 0; i < constraints.size(); ++i) {
            Constraint constraint = (Constraint) constraints.get(i);
            constraint.checkDuplicates(constraints.subList(0, i));
        }
    }

    private static void checkOnlyOnePrimaryKey(
        ImmutableList constraints) {
        int keys = 0;
        for (int i = 0; i < constraints.size(); ++i) {
            Constraint constraint = (Constraint) constraints.get(i);
            if (constraint instanceof PrimaryKey) {
                ++keys;
            }
        }
        if (keys > 1) {
            /* We don't have table name and location, to provide
               a suitable message for the real case.  So this check
               is just as a backup.  */
            throw new MayflyInternalException(
                "attempt to define " + keys + " primary keys");
        }
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

    public boolean canBeTargetOfForeignKey(String targetColumn) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            if (constraint.canBeTargetOfForeignKey(targetColumn)) {
                return true;
            }
        }
        return false;
    }

    public int constraintCount() {
        return constraints.size();
    }
    
    public Constraint constraint(int index) {
        return (Constraint) constraints.get(index);
    }

    public boolean hasPrimaryKey() {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            if (constraint instanceof PrimaryKey) {
                return true;
            }
        }
        return false;
    }

    public boolean refersTo(String table, Evaluator evaluator) {
        evaluator.store().table(table);
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            if (constraint.refersTo(table, evaluator)) {
                return true;
            }
        }
        return false;
    }

    public int requiredInsertionOrder(Row first, Row second) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            int requiredOrder = constraint.requiredInsertionOrder(first, second);
            if (requiredOrder != 0) {
                return requiredOrder;
            }
        }
        return 0;
    }

}
