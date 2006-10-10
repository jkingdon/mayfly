package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.What;

/**
 * Not yet immutable, because of {@link GroupByKeys}
 */
public interface Aggregator {

    public abstract ResultRows group(ResultRows rows, What what, Selected selected);

    public abstract void check(Row dummyRow, Selected selected);

}
