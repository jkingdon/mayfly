package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.what.What;

/**
 * Not yet immutable, because of {@link GroupByKeys}
 */
public interface Aggregator {

    public abstract Rows group(Rows rows, What what, Selected selected);

    public abstract void check(Row dummyRow, Selected selected);

}
