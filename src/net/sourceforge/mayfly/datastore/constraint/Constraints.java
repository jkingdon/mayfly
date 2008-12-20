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

public class Constraints implements Iterable<Constraint> {

    private final ImmutableList<Constraint> constraints;

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
    public void check(Rows rows, Row newRow, TableReference table, Location location) {
        for (Constraint constraint : constraints) {
            constraint.check(rows, newRow, table, location);
        }
    }

    public void checkInsert(
        DataStore store, String schema, String table, Row proposedRow, Location location) {
        for (Constraint constraint : constraints) {
            constraint.checkInsert(store, schema, table, proposedRow, location);
        }
    }

    public DataStore checkDelete(
        DataStore store, String schema, String table, 
        Row rowToDelete, Row replacementRow) {
        for (Constraint constraint : constraints) {
            store = constraint.checkDelete(store, schema, table, 
                rowToDelete, replacementRow);
        }
        return store;
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        for (Constraint constraint : constraints) {
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
        for (Constraint constraint : constraints) {
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

    public Constraints renameColumn(String oldName, String newName) {
        List newConstraints = new ArrayList();
        for (Constraint constraint : constraints) {
            if (constraint.refersTo(oldName)) {
                throw new MayflyException("cannot rename column " + oldName + 
                    " because a constraint refers to it");
            }

            Constraint newConstraint = 
                constraint.renameColumn(oldName, newName);
            newConstraints.add(newConstraint);
        }
        return new Constraints(new ImmutableList(newConstraints));
    }

    public Constraints renameTable(String oldName, String newName) {
        boolean somethingChanged = false;

        List newConstraints = new ArrayList();
        for (Constraint constraint : constraints) {
            Constraint newConstraint = 
                constraint.renameTable(oldName, newName);
            if (newConstraint != constraint) {
                somethingChanged = true;
            }
            newConstraints.add(newConstraint);
        }
        if (somethingChanged) {
            return new Constraints(new ImmutableList(newConstraints));
        }
        else {
            return this;
        }
    }

    public Constraints dropForeignKey(String constraintName) {
        return new Constraints(
            constraintsWithout(constraintName, ForeignKey.class));
    }
    
    public Constraints dropConstraint(String constraintName) {
        return new Constraints(constraintsWithout(constraintName));
    }

    private ImmutableList constraintsWithout(String constraintName) {
        List keys = new ArrayList();
        findConstraint(constraintName, keys);
        return new ImmutableList(keys);
    }

    private ImmutableList constraintsWithout(String constraintName, Class constraintType) {
        List keys = new ArrayList();
        Constraint found = findConstraint(constraintName, keys);
        if (found.getClass() != constraintType) {
            throw new MayflyException(
                "constraint " + constraintName + 
                " is not a " + describe(constraintType));
        }
        return new ImmutableList(keys);
    }

    private Constraint findConstraint(String constraintName, List keys) {
        Constraint found = null;
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
        return found;
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
        return constraints.get(index);
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

    public List referencedTables(Evaluator evaluator) {
        List result = new ArrayList();
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            result.addAll(constraint.referencedTables());
        }
        return result;
    }

    public boolean mustInsertBefore(Row first, Row second) {
        for (Iterator iter = constraints.iterator(); iter.hasNext();) {
            Constraint constraint = (Constraint) iter.next();
            boolean requiredOrder = constraint.mustInsertBefore(first, second);
            if (requiredOrder) {
                return requiredOrder;
            }
        }
        return false;
    }

    public Iterator<Constraint> iterator() {
        return constraints.iterator();
    }

}
