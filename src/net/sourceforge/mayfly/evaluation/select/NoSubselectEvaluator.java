package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.DataStore;


public class NoSubselectEvaluator extends Evaluator {

    public DataStore store() {
        throw new UnimplementedException(
            "subselects are not yet implemented in this context");
    }
    
    public String currentSchema() {
        throw new UnimplementedException(
            "subselects are not yet implemented in this context");
    }

}
