package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.RealChecker;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.select.OptimizedSelect;
import net.sourceforge.mayfly.evaluation.select.Select;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Iterator;

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

        OptimizedSelect optimized = subselect.makeOptimized(evaluator);

        ImmutableList<String> names = 
            columnNames != null ? columnNames : store.table(table).columnNames();
        checkColumnCount(names, optimized.selected);

        MayflyResultSet rows = optimized.asResultSet();
        while (rows.next()) {
            ValueList values = rows.asValues(subselect.location);
            store = Insert.addOneRow(store, table, columnNames, values, checker);
        }
        return store;
    }

    private void checkColumnCount(ImmutableList<String> columnNames,
        Selected selected) {
        if (columnNames.size() != selected.size()) {
            if (selected.size() > columnNames.size()) {
                throw makeException("Too many values.\n", columnNames, selected);
            } else {
                throw makeException("Too few values.\n", columnNames, selected);
            }
        }
    }

    private MayflyException makeException(String message, 
        ImmutableList<String> columnNames, Selected selected) {
        return new MayflyException(
            message + describeNamesAndValues(columnNames, selected),
            subselect.location);
    }

    private String describeNamesAndValues(
        ImmutableList<String> columns, Selected selected) {
        StringBuilder result = new StringBuilder();
        
        result.append("Columns and values from subselect were:\n");

        Iterator<String> nameIterator = columns.iterator();
        Iterator<Expression> valueIterator = selected.iterator();
        while (nameIterator.hasNext() || valueIterator.hasNext()) {
            if (nameIterator.hasNext()) {
                String columnName = nameIterator.next();
                result.append(columnName);
            } else {
                result.append("(none)");
            }

            result.append(' ');

            if (valueIterator.hasNext()) {
                Expression value = valueIterator.next();
                result.append(value.displayName());
            } else {
                result.append("(none)");
            }
            
            result.append('\n');
        }
        return result.toString();
    }

}
