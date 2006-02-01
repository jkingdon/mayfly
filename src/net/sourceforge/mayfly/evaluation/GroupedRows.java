package net.sourceforge.mayfly.evaluation;

import java.util.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.evaluation.what.Selected;
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

    public Rows ungroup(Selected selected) {
        Rows result = new Rows();

        Iterator iter = groups.keySet().iterator();
        while (iter.hasNext()) {
            List keys = (List) iter.next();
            result = (Rows) result.with(rowForKey(keys, getRows(keys), selected));
        }

        return result;
    }

    private Row rowForKey(List keys, Rows rowsForKey, Selected selected) {
        Map allKeyValues = allKeyValues(keys);

        TupleBuilder builder = new TupleBuilder();
        addColumnsForWhat(rowsForKey, selected, allKeyValues, builder);
        addColumnsForKeys(allKeyValues, builder);
        return new Row(builder);
    }

    private void addColumnsForWhat(Rows rowsForKey, Selected selected, Map allKeyValues, TupleBuilder builder) {
        for (int i = 0; i < selected.size(); ++i) {
            Expression expression = selected.element(i);
            Column found = lookupColumn(expression);
            if (found != null) {
                /** Hmm.  Is there any reason we want to add these here
                  * rather than just let {@link #addColumnsForKeys(Map, TupleBuilder)} do it?
                  */
                builder.append(found, (Cell) allKeyValues.get(found));
                allKeyValues.remove(found);
            }
            else if (expression.firstAggregate() != null) {
                Cell aggregated = expression.aggregate(rowsForKey);
                builder.append(new PositionalHeader(i), aggregated);
            }
            else {
                throw new MayflyException(expression.displayName() + " is not aggregate or mentioned in GROUP BY");
            }
        }
    }

    private void addColumnsForKeys(Map allKeyValues, TupleBuilder builder) {
        for (Iterator iter = allKeyValues.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            builder.append((Column) entry.getKey(), (Cell) entry.getValue());
        }
    }

    private Map allKeyValues(List keys) {
        Map allKeyValues = new LinkedHashMap();
        if (keyColumns.size() != keys.size()) {
            throw new MayflyInternalException(
                "keyColumns has " + keyColumns.size() + " keys but keys has " + keys.size());
        }
        for (int i = 0; i < keyColumns.size(); ++i) {
            allKeyValues.put(keyColumns.get(i), keys.get(i));
        }
        return allKeyValues;
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
