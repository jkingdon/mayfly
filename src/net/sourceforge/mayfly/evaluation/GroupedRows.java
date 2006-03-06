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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedRows {
    
    private Map groups = new LinkedHashMap();
    private List/*<Column>*/ keyColumns = null;
    private GroupByKeys keys;

    public int groupCount() {
        return groups.size();
    }

    public void add(GroupByKeys keys, Row row) {
        GroupByCells cells = keys.evaluate(row);
        addRowToGroup(cells, row);
        this.keys = keys;
        this.keyColumns = keys.columns(row);
    }

    private void addRowToGroup(GroupByCells keys, Row row) {
        Rows start;
        if (groups.containsKey(keys)) {
            start = (Rows) groups.get(keys);
        }
        else {
            start = new Rows();
        }
        
        Rows modified = (Rows) start.with(row);
        groups.put(keys, modified);
    }

    public Iterator iteratorForFirstKeys() {
        List firstKeys = new ArrayList();
        for (Iterator iter = groups.keySet().iterator(); iter.hasNext();) {
            GroupByCells keys = (GroupByCells) iter.next();
            firstKeys.add(keys.firstKey());
        }
        return firstKeys.iterator();
    }

    public Rows getRows(GroupByCells keys) {
        return (Rows) groups.get(keys);
    }

    public Rows ungroup(Selected selected) {
        Rows result = new Rows();

        Iterator iter = groups.keySet().iterator();
        while (iter.hasNext()) {
            GroupByCells keys = (GroupByCells) iter.next();
            result = (Rows) result.with(rowForKey(keys, getRows(keys), selected));
        }

        return result;
    }

    private Row rowForKey(GroupByCells cells, Rows rowsForKey, Selected selected) {
        TupleBuilder builder = new TupleBuilder();
        addColumnsForWhat(rowsForKey, selected, builder);
        addColumnsForKeys(cells, selected, builder);
        return new Row(builder);
    }

    private void addColumnsForWhat(Rows rowsForKey, Selected selected, TupleBuilder builder) {
        for (int i = 0; i < selected.size(); ++i) {
            Expression expression = selected.element(i);
            if (keys.containsExpresion(expression)) {
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

    private void addColumnsForKeys(GroupByCells cells, Selected selected, TupleBuilder builder) {
        if (keys.keyCount() != cells.size()) {
            throw new MayflyInternalException(
                "have " + keyColumns.size() + " columns but " + cells.size() + " values");
        }
        for (int i = 0; i < cells.size(); ++i) {
            builder.append((Column) keyColumns.get(i), cells.get(i));
        }
    }

//    private CellHeader makePositionalHeader(Selected selected, int keyIndex) {
//        Expression expression = keys.get(keyIndex).expression();
//        return new ExpressionHeader(expression);
//    }

}
