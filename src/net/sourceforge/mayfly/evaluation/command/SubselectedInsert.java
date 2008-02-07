package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.RealChecker;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.select.Select;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

public class SubselectedInsert extends Command {

    private final UnresolvedTableReference unresolvedTable;
    private final ImmutableList columnNames;

    private final Location location;
    private final Select subselect;

    public SubselectedInsert(
        UnresolvedTableReference table, ImmutableList columnNames, 
        Select subselect) {
        this.unresolvedTable = table;
        this.columnNames = columnNames;
        this.subselect = subselect;
        this.location = table.location;
    }

    @Override
    public UpdateStore update(Evaluator evaluator) {
        TableReference resolved = unresolvedTable.resolve(evaluator);
        Checker checker = new RealChecker(evaluator.store(), resolved, 
            location, unresolvedTable.options);

        return new UpdateStore(
            insertRows(evaluator, resolved, checker),
            1,
            checker.newIdentityValue()
        );
    }

    @Override
    public UpdateStore update(DataStore store, String currentSchema) {
        throw new MayflyInternalException("should call the other update");
    }

    private DataStore insertRows(Evaluator evaluator, TableReference table, 
        Checker checker) {
        DataStore store = evaluator.store();

        MayflyResultSet rows = subselect.select(evaluator, null);
        while (rows.next()) {
            ValueList values = rows.asValues(subselect.location);
            store = Insert.addOneRow(store, table, columnNames, values, checker);
        }
        return store;
    }

}
