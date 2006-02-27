package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupByKeys extends ValueObject {

    private List items = new ArrayList();

    public void add(GroupItem key) {
        items.add(key);
    }

    public Iterator itemIterator() {
        return items.iterator();
    }

    List columns(Row row) {
        List columns = new ArrayList();
        for (Iterator iter = itemIterator(); iter.hasNext();) {
            GroupItem item = (GroupItem) iter.next();
            columns.add(item.column().lookup(row));
        }
        return columns;
    }

    public List evaluate(Row row) {
        List keyCells = new ArrayList();
        for (Iterator iter = itemIterator(); iter.hasNext();) {
            GroupItem item = (GroupItem) iter.next();
            keyCells.add(item.expression().evaluate(row));
        }
        return keyCells;
    }

}
