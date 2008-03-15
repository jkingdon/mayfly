package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.parser.Location;

import java.io.IOException;
import java.io.Writer;

public class NullConstraint extends Constraint {

    public NullConstraint() {
        super(null);
    }

    @Override
    public void check(Rows existingRows, Row proposedRow, TableReference table,
        Location location) {
    }

    @Override
    public boolean checkDropColumn(TableReference table, String column) {
        throw new UnimplementedException();
    }

    @Override
    public void checkExistingRows(DataStore store, TableReference table) {
    }

    @Override
    public void dump(Writer out) throws IOException {
    }

    @Override
    public Constraint renameColumn(String oldName, String newName) {
        throw new UnimplementedException();
    }

}
