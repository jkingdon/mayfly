package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.NoColumn;
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

    public Cell lookup(String tableOrAlias, String columnName, Location location) {
        throw new NoColumn(tableOrAlias, columnName, location);
    }

}
