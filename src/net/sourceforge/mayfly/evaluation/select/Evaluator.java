package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.from.FromTable;
import net.sourceforge.mayfly.parser.Location;

/**
 * @internal
 * This class has the information needed to evaluate a subselect.
 */
public abstract class Evaluator {
    
    public static final Evaluator NO_SUBSELECT_NEEDED = 
        new NoSubselectEvaluator();

    abstract public DataStore store();

    abstract public String currentSchema();

    public Cell lookup(ResultRow row, 
        String tableOrAlias, String columnName, Location location) {
        Expression found = row.findColumn(tableOrAlias, columnName, location);
        return row.findValue(found);
    }

    public TableData table(FromTable table) {
        return store().table(currentSchema(), table.tableName);
    }

    public Options options() {
        return new Options();
    }
    
}
