package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

public interface Aggregator {

    public abstract Rows group(Rows rows, What what, Selected selected);

    public abstract void check(Row dummyRow, What what);

}
