package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.util.ValueObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GroupByKeys extends ValueObject {

    private List items = new ArrayList();

    public void add(GroupItem key) {
        items.add(key);
    }

    List expressions() {
        List columns = new ArrayList();
        for (Iterator iter = items.iterator(); iter.hasNext();) {
            GroupItem item = (GroupItem) iter.next();
            columns.add(item.expression());
        }
        return Collections.unmodifiableList(columns);
    }

    public GroupByCells evaluate(ResultRow row) {
        GroupByCells keyCells = new GroupByCells();
        for (Iterator iter = items.iterator(); iter.hasNext();) {
            GroupItem item = (GroupItem) iter.next();
            keyCells.add(item.expression().evaluate(row));
        }
        return keyCells;
    }

    public int keyCount() {
        return items.size();
    }

    public boolean containsExpresion(Expression target) {
        for (Iterator iter = items.iterator(); iter.hasNext();) {
            GroupItem item = (GroupItem) iter.next();
            if (target.sameExpression(item.expression())) {
                return true;
            }
        }
        return false;
    }

    public void resolve(ResultRow row) {
        for (Iterator iter = items.iterator(); iter.hasNext();) {
            GroupItem item = (GroupItem) iter.next();
            item.resolve(row);
        }
    }

    public int size() {
        return items.size();
    }

    public GroupItem get(int index) {
        return (GroupItem) items.get(index);
    }

}
