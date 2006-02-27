package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.PositionalHeader;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.Rows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedRows {
    
    private Map/*<List<Cell>, Rows>*/ groups = new LinkedHashMap();
    private List/*<Column>*/ keyColumns = null;
    //private GroupByKeys keys;

    public int groupCount() {
        return groups.size();
    }

    public void add(GroupByKeys keys, Row row) {
        addRowToGroup(keys.evaluate(row), row);
        //this.keys = keys;
        this.keyColumns = keys.columns(row);
    }

    private void addRowToGroup(List groupCells, Row row) {
        Rows start;
        if (groups.containsKey(groupCells)) {
            start = (Rows) groups.get(groupCells);
        }
        else {
            start = new Rows();
        }
        
        Rows modified = (Rows) start.with(row);
        groups.put(groupCells, modified);
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
        addColumnsForWhat(rowsForKey, selected, builder);
        addColumnsForKeys(allKeyValues, builder);
        return new Row(builder);
    }

    private void addColumnsForWhat(Rows rowsForKey, Selected selected, TupleBuilder builder) {
        for (int i = 0; i < selected.size(); ++i) {
            Expression expression = selected.element(i);
            Column found = lookupColumn(expression);
            if (found != null) {
                /** Just let {@link #addColumnsForKeys(Map, TupleBuilder)} add it. */
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

    private Column lookupColumn(Expression element) {
        for (Iterator iter = keyColumns.iterator(); iter.hasNext();) {
            Column candidate = (Column) iter.next();
            if (element.matches(candidate)) {
                return candidate;
            }
        }
        return null;
    }

}
