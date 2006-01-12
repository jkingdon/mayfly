package net.sourceforge.mayfly.evaluation;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class GroupedRows {
    
    Map/*<List<Cell>, Rows>*/ groups = new LinkedHashMap();
    List/*<Column>*/ keyColumns = null;

    public int groupCount() {
        return groups.size();
    }

    public void add(Column key, Row row) {
        add(Collections.singletonList(key), row);
    }

    public void add(List keyColumns, Row row) {
        List keyCells = new ArrayList();
        for (int i = 0; i < keyColumns.size(); ++i) {
            keyCells.add(row.cell((Column) keyColumns.get(i)));
        }

        Rows start;
        if (groups.containsKey(keyCells)) {
            start = (Rows) groups.get(keyCells);
        }
        else {
            start = new Rows();
        }
        
        Rows modified = (Rows) start.with(row);
        groups.put(keyCells, modified);
        this.keyColumns = keyColumns;
    }

    public Iterator keyIterator() {
        List singleKeys = new ArrayList();
        for (Iterator iter = groups.keySet().iterator(); iter.hasNext();) {
            List keys = (List) iter.next();
            singleKeys.add(keys.get(0));
        }
        return singleKeys.iterator();
    }

    public Rows getRows(Cell key) {
        return (Rows) groups.get(Collections.singletonList(key));
    }

    public Rows getRows(List/*<Cell>*/ keys) {
        return (Rows) groups.get(keys);
    }

    public Rows ungroup(What what) {
        Rows result = new Rows();

        Iterator iter = groups.keySet().iterator();
        while (iter.hasNext()) {
            List keys = (List) iter.next();
            result = (Rows) result.with(rowForKey(getRows(keys), what));
        }

        return result;
    }

    private Row rowForKey(Rows rowsForKey, What what) {
        Row sampleRow = (Row) rowsForKey.iterator().next();

        TupleBuilder builder = new TupleBuilder();
        for (int i = 0; i < what.size(); ++i) {
            WhatElement element = (WhatElement) what.element(i);
            Column found = lookupColumn(element);
            if (found != null) {
                builder.append(found, sampleRow.cell(found));
            }
            else if (element.firstAggregate() != null) {
                Cell aggregated = element.aggregate(rowsForKey);
                builder.append(new PositionalHeader(i), aggregated);
            }
            else {
                throw new MayflyException(element.displayName() + " is not aggregate or mentioned in GROUP BY");
            }
        }
        return new Row(builder);
    }

    private Column lookupColumn(WhatElement element) {
        for (Iterator iter = keyColumns.iterator(); iter.hasNext();) {
            Column candidate = (Column) iter.next();
            if (element.matches(candidate)) {
                return candidate;
            }
        }
        return null;
    }

}
