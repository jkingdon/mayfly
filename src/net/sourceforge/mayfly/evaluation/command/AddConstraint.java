package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.datastore.constraint.Constraint;

public class AddConstraint extends Command {

    private final UnresolvedConstraint constraint;
    private final UnresolvedTableReference table;

    public AddConstraint(
        UnresolvedTableReference table, UnresolvedConstraint constraint) {
        this.table = table;
        this.constraint = constraint;
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        TableReference reference = table.resolve(store, currentSchema, null);
        ConstraintsBuilder builder = ConstraintsBuilder.fromTable(store, reference);
        Constraint resolved = constraint.resolve(builder);
        resolved.checkExistingRows(store, reference);
        return new UpdateStore(
            store.addConstraint(reference, resolved), 
            0);
    }

}
