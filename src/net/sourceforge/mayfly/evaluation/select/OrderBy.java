package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class OrderBy {

    List elements = new ArrayList();

    public OrderBy add(SingleColumn column) {
        return add(new ColumnOrderItem(column, true));
    }

    public OrderBy add(OrderItem item) {
        elements.add(item);
        return this;
    }

    public Iterator iterator() {
        return elements.iterator();
    }

    public ResultRows sort(ResultRows rows, final What what) {
        if (isEmpty()) {
            return rows;
        }

        List rowList = new ArrayList(rows.asList());
        Collections.sort(rowList, new Comparator() {

            public int compare(Object o1, Object o2) {
                ResultRow first = (ResultRow) o1;
                ResultRow second = (ResultRow) o2;
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
        return new ResultRows(new ImmutableList(rowList));
    }

    public void check(ResultRow afterGroupByAndDistinct, 
        ResultRow afterGroupBy, ResultRow afterJoins) {
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            OrderItem item = (OrderItem) iter.next();
            item.check(afterGroupByAndDistinct, afterGroupBy, afterJoins);
        }
    }

    public boolean isEmpty() {
        return elements.size() == 0;
    }

}
