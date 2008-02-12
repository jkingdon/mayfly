package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GroupByKeys {

    private List<GroupItem> items = new ArrayList<GroupItem>();

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
        List<Cell> keyCells = new ArrayList<Cell>();
        for (GroupItem item : items) {
            keyCells.add(item.expression().evaluate(row));
        }
        return new GroupByCells(keyCells);
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
        return items.get(index);
    }

}
