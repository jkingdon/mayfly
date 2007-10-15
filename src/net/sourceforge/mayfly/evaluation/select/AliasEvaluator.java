package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.from.FromTable;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.parser.Location;

public class AliasEvaluator extends Evaluator {

    private final What what;
    private final Evaluator delegate;

    public AliasEvaluator(What selected) {
        this(selected, Evaluator.NO_SUBSELECT_NEEDED);
    }

    public AliasEvaluator(What selected, Evaluator evaluator) {
        this.what = selected;
        this.delegate = evaluator;
    }

    @Override
    public String currentSchema() {
        return delegate.currentSchema();
    }

    @Override
    public DataStore store() {
        return delegate.store();
    }
    
    @Override
    public Cell lookup(ResultRow row, String tableOrAlias, String columnName, Location location) {
        if (tableOrAlias != null) {
            return delegate.lookup(row, tableOrAlias, columnName, location);
        }

        Expression aliasedTo = what.lookupAlias(columnName);
        if (aliasedTo != null) {
            return aliasedTo.evaluate(row);
        }
        else {
            return delegate.lookup(row, tableOrAlias, columnName, location);
        }
    }
    
    @Override
    public TableData table(FromTable table) {
        return delegate.table(table);
    }
    
    @Override
    public Options options() {
        return delegate.options();
    }

}
