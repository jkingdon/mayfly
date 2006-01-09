package net.sourceforge.mayfly.ldbc;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

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
