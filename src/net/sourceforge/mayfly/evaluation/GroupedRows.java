package net.sourceforge.mayfly.evaluation;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class GroupedRows {
    
    Map groups = new LinkedHashMap();
    Column keyColumn = null;

    public int groupCount() {
        return groups.size();
    }

    public void add(Column keyColumn, Cell key, Row row) {
        Rows start;
        if (groups.containsKey(key)) {
            start = (Rows) groups.get(key);
        }
        else {
            start = new Rows();
        }

        Rows modified = (Rows) start.with(row);
        groups.put(key, modified);
        this.keyColumn = keyColumn;
    }

    public Iterator keyIterator() {
        return Collections.unmodifiableSet(groups.keySet()).iterator();
    }

    public Rows getRows(Cell key) {
        return (Rows) groups.get(key);
    }

    public Rows ungroup(What what) {
        Rows result = new Rows();

        Iterator iter = groups.keySet().iterator();
        while (iter.hasNext()) {
            Cell key = (Cell) iter.next();
            result = (Rows) result.with(rowForKey(key, getRows(key), what));
        }

        return result;
    }

    private Row rowForKey(Cell key, Rows rowsForKey, What what) {
        TupleBuilder builder = new TupleBuilder();
        for (int i = 0; i < what.size(); ++i) {
            WhatElement element = (WhatElement) what.element(i);
            if (element.matches(keyColumn)) {
                builder.append(keyColumn, key);
            }
            else if (element.firstAggregate() != null) {
                Cell aggregated = element.aggregate(rowsForKey);
                builder.append(new PositionalHeader(i), aggregated);
            }
            else {
                // This message needs to identify which WhatElement caused
                // the problem, and in general be better worded than this.
                throw new MayflyException("not the key or an aggregate");
            }
        }
        Row row = new Row(builder);
        return row;
    }

}
