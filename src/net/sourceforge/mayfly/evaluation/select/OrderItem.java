package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.ldbc.what.What;
import net.sourceforge.mayfly.util.ValueObject;

abstract public class OrderItem extends ValueObject {

    private final boolean ascending;

    protected OrderItem(boolean ascending) {
        this.ascending = ascending;
    }

    public int compareRows(What what, Row first, Row second) {
        int comparison = compareAscending(what, first, second);
        return ascending ? comparison : - comparison;
    }

    abstract protected int compareAscending(What what, Row first, Row second);

    abstract public void check(Row dummyRow);

}
