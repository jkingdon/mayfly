package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.from.FromTable;
import net.sourceforge.mayfly.parser.Location;

public class RowEvaluator extends Evaluator {

    private final Evaluator nestedEvaluator;
    private final ResultRow row;

    public RowEvaluator(ResultRow row, Evaluator nestedEvaluator) {
        this.row = row;
        this.nestedEvaluator = nestedEvaluator;
    }

    @Override
    public String currentSchema() {
        return nestedEvaluator.currentSchema();
    }

    @Override
    public DataStore store() {
        return nestedEvaluator.store();
    }
    
    @Override
    public Cell lookup(ResultRow row, 
        String tableOrAlias, String columnName, Location location) {
        try {
            return super.lookup(row, tableOrAlias, columnName, location);
        }
        catch (NoColumn e) {
            return nestedEvaluator.lookup(this.row, tableOrAlias, columnName, location);
        }
    }
    
    @Override
    public Expression lookupName(String columnName) {
        Expression found = row.findColumnOrNull(null, columnName, Location.UNKNOWN);
        return found;
//        return nestedEvaluator.lookupName(columnName);
    }
    
    @Override
    public Options options() {
        return nestedEvaluator.options();
    }
    
    @Override
    public TableData table(FromTable table) {
        return nestedEvaluator.table(table);
    }

}
