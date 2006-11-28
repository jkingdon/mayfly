package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.Expression;
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
    
    public Cell lookup(String tableOrAlias, String columnName, Location location) {
        Expression found = row.findColumn(tableOrAlias, columnName, location);
        return row.findValue(found);
    }

}
