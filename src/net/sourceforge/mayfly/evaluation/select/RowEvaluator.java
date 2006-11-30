package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Location;

public class RowEvaluator extends Evaluator {

    private final Evaluator nestedEvaluator;
    private final ResultRow row;

    public RowEvaluator(ResultRow row, Evaluator nestedEvaluator) {
        this.row = row;
        this.nestedEvaluator = nestedEvaluator;
    }

    public String currentSchema() {
        return nestedEvaluator.currentSchema();
    }

    public DataStore store() {
        return nestedEvaluator.store();
    }
    
    public Cell lookup(ResultRow row, 
        String tableOrAlias, String columnName, Location location) {
        try {
            return super.lookup(row, tableOrAlias, columnName, location);
        }
        catch (NoColumn e) {
            return nestedEvaluator.lookup(this.row, tableOrAlias, columnName, location);
        }
    }

}
