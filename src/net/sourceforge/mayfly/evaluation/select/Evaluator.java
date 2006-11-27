package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.DataStore;

/**
 * @internal
 * This class has the information needed to evaluate a subselect.
 */
public abstract class Evaluator {
    
    public static final Evaluator NO_SUBSELECT_NEEDED = 
        new NoSubselectEvaluator();

    abstract public DataStore store();

    abstract public String currentSchema();

}
