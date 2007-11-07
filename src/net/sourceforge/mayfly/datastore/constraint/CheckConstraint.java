package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.parser.Location;

import java.io.IOException;
import java.io.Writer;

public class CheckConstraint extends Constraint {

    private final Condition condition;
    private final String tableName;

    public CheckConstraint(Condition condition, String tableName, String constraintName) {
        super(constraintName);
        this.condition = condition;
        this.tableName = tableName;
    }

    @Override
    public void check(Rows existingRows, Row proposedRow, Location location) {
        /* Our message needs to be more informative somehow.  Giving the
           constraint name of the constraint would be one step.  If there
           isn't one, then what?  The text of the expression (which we currently
           don't have a way to get)?  */
        if (!condition.evaluate(proposedRow, tableName)) {
            throw new MayflyException(
                "cannot insert into " + tableName + "; " +
                "check constraint failed", 
//                condition.source() + " is false", 
                location);
        }
    }

    @Override
    public boolean checkDropColumn(TableReference table, String column) {
        throw new UnimplementedException();
    }
    
    @Override
    public Constraint renameColumn(String oldName, String newName) {
        throw new UnimplementedException();
    }

    @Override
    public void checkExistingRows(DataStore store, TableReference table) {
        throw new UnimplementedException();
    }

    @Override
    public void dump(Writer out) throws IOException {
        throw new UnimplementedException();
    }

}
