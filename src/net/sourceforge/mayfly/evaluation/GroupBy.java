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
        List columns = findColumns(rows);

        GroupedRows grouped = new GroupedRows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            grouped.add(columns, row);
        }
        return grouped;
    }

    private List findColumns(Rows rows) {
        if (rows.size() > 0) {
            Row sampleRow = (Row) rows.iterator().next();

            List columns = new ArrayList();
            for (Iterator iter = items.iterator(); iter.hasNext();) {
                GroupItem item = (GroupItem) iter.next();
                columns.add(item.column().lookup(sampleRow));
            }
            return columns;
        }
        else {
            return null;
        }
    }

    public Rows group(Rows rows, What what, What selected) {
        return makeGroupedRows(rows).ungroup(what);
    }
    
    public void check(Row dummyRow, What what, What selected) {
        makeGroupedRows(new Rows(dummyRow)).ungroup(what);
    }
    
}