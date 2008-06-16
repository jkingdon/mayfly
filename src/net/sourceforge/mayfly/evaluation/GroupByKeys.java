package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupByKeys {

    private final ImmutableList<GroupItem> items;

    public GroupByKeys(GroupItem... items) {
        this(ImmutableList.fromArray(items));
    }

    public GroupByKeys(ImmutableList<GroupItem> items) {
        this.items = items;
    }

    List expressions() {
        List columns = new ArrayList();
        for (GroupItem item : items) {
            columns.add(item.expression());
        }
        return Collections.unmodifiableList(columns);
    }

    public GroupByCells evaluate(ResultRow row, Evaluator evaluator) {
        List<Cell> keyCells = new ArrayList<Cell>();
        for (GroupItem item : items) {
            keyCells.add(item.expression().evaluate(row, evaluator));
        }
        return new GroupByCells(keyCells);
    }

    public int keyCount() {
        return items.size();
    }

    public boolean containsExpresion(Expression target) {
        for (GroupItem item : items) {
            if (target.sameExpression(item.expression())) {
                return true;
            }
        }
        return false;
    }

    public GroupByKeys resolve(ResultRow row, Evaluator evaluator) {
        List<GroupItem> resolvedItems = new ArrayList<GroupItem>();
        boolean changed = false;
        for (GroupItem item : items) {
            GroupItem resolved = item.resolve(row, evaluator);
            if (resolved != item) {
                changed = true;
            }
            resolvedItems.add(resolved);
        }
        if (changed) {
            return new GroupByKeys(new ImmutableList(resolvedItems));
        }
        else {
            return this;
        }
    }

    public int size() {
        return items.size();
    }

    public GroupItem get(int index) {
        return items.get(index);
    }

}
