package net.sourceforge.mayfly.evaluation;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class GroupBy extends ValueObject {
    
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
    
    public boolean hasGroups() {
        return !items.isEmpty();
    }
    
    public Rows group(Rows rows, What what, What selected) {
        if (hasGroups()) {
            return makeGroupedRows(rows).ungroup(what);
        }
        else {
            if (isAggregate(selected)) {
                return selected.aggregate(rows);
            }
            return rows;
        }
    }
    
    private boolean isAggregate(What selected) {
        String firstColumn = null;
        String firstAggregate = null;

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            WhatElement element = (WhatElement) iter.next();
            if (firstColumn == null) {
                firstColumn = element.firstColumn();
            }
            if (firstAggregate == null) {
                firstAggregate = element.firstAggregate();
            }
            
            if (firstColumn != null && firstAggregate != null) {
                throw new MayflyException(firstColumn + " is a column but " + firstAggregate + " is an aggregate");
            }
        }
        return firstAggregate != null;
    }
    
}
