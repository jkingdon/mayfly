package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

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
