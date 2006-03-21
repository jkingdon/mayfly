package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.evaluation.ColumnOrderItem;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.what.What;
import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class OrderBy extends Aggregate {

    private List elements = new ArrayList();

    public OrderBy add(SingleColumn column) {
        return add(new ColumnOrderItem(column, true));
    }

    public OrderBy add(OrderItem item) {
        elements.add(item);
        return this;
    }

    protected Aggregate createNew(Iterable items) {
        throw new UnimplementedException();
    }

    public Iterator iterator() {
        return elements.iterator();
    }

    public Rows sort(final DataStore store, Rows rows, final What what) {
        if (isEmpty()) {
            return rows;
        }

        List rowList = new L(rows);
        Collections.sort(rowList, new Comparator() {

            public int compare(Object o1, Object o2) {
                Row first = (Row) o1;
                Row second = (Row) o2;
                for (Iterator iter = elements.iterator(); iter.hasNext();) {
                    OrderItem item = (OrderItem) iter.next();
                    int comparison = item.compareRows(what, first, second);
                    if (comparison != 0) {
                        return comparison;
                    }
                }
                return 0;
            }
            
        });
        return new Rows(new ImmutableList(rowList));
    }

    public void check(Row dummyRow) {
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            OrderItem item = (OrderItem) iter.next();
            item.check(dummyRow);
        }
    }

    public boolean isEmpty() {
        return elements.size() == 0;
    }

}
