package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.what.What;

public interface Aggregator {

    public abstract Rows group(Rows rows, What what, Selected selected);

    public abstract void check(Row dummyRow, Selected selected);

}
