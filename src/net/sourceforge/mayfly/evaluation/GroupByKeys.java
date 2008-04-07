package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @internal
 * Not yet immutable, because of {@link GroupItem}
 */
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

    public void resolve(ResultRow row, Evaluator evaluator) {
        for (GroupItem item : items) {
            item.resolve(row, evaluator);
        }
    }

    public int size() {
        return items.size();
    }

    public GroupItem get(int index) {
        return items.get(index);
    }

}
