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
   @internal
   This class is responsible for looking up names and also keeping a hold
   of the store and current schema (I guess because the two tasks seemed
   to need the same information).
   
   Cases for evaluating a name are:
   
   - column name, directly
   
   - value from the outer query of a correlated subquery, for example
   candidate.region in:
   SELECT name FROM countries candidate
     WHERE population >= 
       (SELECT max(population) FROM countries other
         WHERE other.region = candidate.region)

   - column alias (total in select a + b as total).
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
