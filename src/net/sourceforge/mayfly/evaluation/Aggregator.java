package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

public interface Aggregator {

    public abstract Rows group(Rows rows, What what, What selected);

}
