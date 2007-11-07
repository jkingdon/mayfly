package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.RealChecker;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

public class Insert extends Command {

    public final UnresolvedTableReference table;
    public final ImmutableList columnNames;
    public final ValueList values;
    public final Location location;

    public Insert(UnresolvedTableReference table, 
        ImmutableList columnNames, ValueList values,
        Location location) {
        this.table = table;
        this.columnNames = columnNames;
        this.values = values;
        this.location = location;
    }

    public String table() {
        return table.tableName();
    }

    @Override
    public UpdateStore update(DataStore store, String defaultSchema) {
        TableReference resolved = table.resolve(store, defaultSchema, null);
        Checker checker = new RealChecker(store, resolved, location, table.options);

        return new UpdateStore(
            insertOneRow(store, resolved, checker),
            1,
            checker.newIdentityValue()
        );
    }

    private DataStore insertOneRow(DataStore store, TableReference resolved, 
        Checker checker) {
        if (columnNames == null) {
            return store.addRow(resolved.schema(), resolved.tableName(), 
                values, checker);
        }
        else {
            return store.addRow(resolved.schema(), resolved.tableName(), 
                columnNames, values, checker);
        }
    }

}
