package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class GroupBy extends ValueObject implements Aggregator {
    
    private List items = new ArrayList();

    public void add(GroupItem item) {
        items.add(item);
    }

    public GroupedRows makeGroupedRows(Rows rows) {
        SingleColumn column = oneColumn();

        GroupedRows grouped = new GroupedRows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            Column foundColumn = column.lookup(row);
            Cell key = column.evaluate(row);
            grouped.add(foundColumn, key, row);
        }
        return grouped;
    }

    private SingleColumn oneColumn() {
        if (items.size() != 1) {
            throw new IllegalStateException("Expected one group by item but got " + items.size());
        }
        GroupItem groupItem = (GroupItem) items.get(0);
        return groupItem.column();
    }
    
    public Rows group(Rows rows, What what, What selected) {
        return makeGroupedRows(rows).ungroup(what);
    }
    
}
