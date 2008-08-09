package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.what.What;

/**
 * @internal
 * Implementations of this class should be immutable.
 */
abstract public class OrderItem {

    private final boolean ascending;

    protected OrderItem(boolean ascending) {
        this.ascending = ascending;
    }

    public int compareRows(What what, Evaluator evaluator, ResultRow first, ResultRow second) {
        int comparison = compareAscending(what, evaluator, first, second);
        return ascending ? comparison : - comparison;
    }

    abstract protected int compareAscending(What what, Evaluator evaluator, ResultRow first, ResultRow second);

    abstract public void check(ResultRow afterGroupByAndDistinct, 
        ResultRow afterGroupBy, ResultRow afterJoins, Evaluator evaluator);

}
