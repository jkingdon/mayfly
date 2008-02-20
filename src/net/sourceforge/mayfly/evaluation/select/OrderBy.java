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

    final ImmutableList<OrderItem> elements;
    
    public OrderBy() {
        this(new ImmutableList<OrderItem>());
    }

    public OrderBy(ImmutableList<OrderItem> elements) {
        this.elements = elements;
    }

    public OrderBy with(SingleColumn column) {
        return with(new ColumnOrderItem(column, true));
    }

    public OrderBy with(OrderItem item) {
        return new OrderBy(elements.with(item));
    }

    public Iterator<OrderItem> iterator() {
        return elements.iterator();
    }

    public ResultRows sort(ResultRows rows, 
        final What what, final Evaluator evaluator) {
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
                    int comparison = item.compareRows(what, evaluator, first, second);
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
        ResultRow afterGroupBy, ResultRow afterJoins, Evaluator evaluator) {
        for (OrderItem item : elements) {
            item.check(afterGroupByAndDistinct, afterGroupBy, 
                afterJoins, evaluator);
        }
    }

    public boolean isEmpty() {
        return elements.size() == 0;
    }

}
