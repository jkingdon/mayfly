package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.what.What;

abstract public class OrderItem {

    private final boolean ascending;

    protected OrderItem(boolean ascending) {
        this.ascending = ascending;
    }

    public int compareRows(What what, ResultRow first, ResultRow second) {
        int comparison = compareAscending(what, first, second);
        return ascending ? comparison : - comparison;
    }

    abstract protected int compareAscending(What what, ResultRow first, ResultRow second);

    abstract public void check(ResultRow afterGroupByAndDistinct, ResultRow afterJoins);

}
