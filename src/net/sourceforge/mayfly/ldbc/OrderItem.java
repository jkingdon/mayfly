package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class OrderItem extends ValueObject {

    private final SingleColumn column;
    private final boolean ascending;

    public OrderItem(SingleColumn column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    public int compareRows(Row first, Row second) {
        Cell cell1 = column.evaluate(first);
        Cell cell2 = column.evaluate(second);
        int comparison = cell1.compareTo(cell2);
        return ascending ? comparison : - comparison;
    }

    public void check(Row dummyRow) {
        column.evaluate(dummyRow);
    }

}
