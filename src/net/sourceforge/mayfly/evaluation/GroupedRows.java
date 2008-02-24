package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupedRows {
    
    private Map groups = new LinkedHashMap();
    private List/*<Expression>*/ keyColumns = null;
    private GroupByKeys keys;

    public int groupCount() {
        return groups.size();
    }

    public void add(GroupByKeys keys, ResultRow row, Evaluator evaluator) {
        ResultRow resultRow = row;

        GroupByCells cells = keys.evaluate(resultRow, evaluator);
        addRowToGroup(cells, resultRow);
        this.keys = keys;
        this.keyColumns = keys.expressions();
    }
    
    public void add(GroupByKeys keys, ResultRow row) {
        add(keys, row, Evaluator.NO_SUBSELECT_NEEDED);
    }

    private void addRowToGroup(GroupByCells keys, ResultRow resultRow) {
        ResultRows start;
        if (groups.containsKey(keys)) {
            start = (ResultRows) groups.get(keys);
        }
        else {
            start = new ResultRows();
        }
        
        ResultRows modified = start.with(resultRow);
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

    public ResultRows getRows(GroupByCells keys) {
        return (ResultRows) groups.get(keys);
    }

    public ResultRows ungroup(Selected selected) {
        ResultRows result = new ResultRows();

        Iterator iter = groups.keySet().iterator();
        while (iter.hasNext()) {
            GroupByCells keys = (GroupByCells) iter.next();
            result = result.with(
                rowForKey(keys, getRows(keys), selected));
        }

        return result;
    }

    private ResultRow rowForKey(
        GroupByCells cells, ResultRows rowsForKey, Selected selected) {
        ResultRow result = new ResultRow();
        result = addColumnsForWhat(rowsForKey, selected, result);
        result = addColumnsForKeys(cells, selected, result);
        return result;
    }

    private ResultRow addColumnsForWhat(
        ResultRows rowsForKey, Selected selected, ResultRow accumulator) {
        for (int i = 0; i < selected.size(); ++i) {
            Expression expression = selected.element(i);
            accumulator = 
                addColumnsForExpression(rowsForKey, expression, accumulator);
        }
        return accumulator;
    }

    private ResultRow addColumnsForExpression(
        ResultRows rowsForKey, Expression expression, ResultRow accumulator) {
        if (keys.containsExpresion(expression)) {
            /** Just let addColumnsForKeys add it. */
        }
        else if (expression.firstAggregate() != null) {
            Cell aggregated = expression.aggregate(rowsForKey);
            accumulator = accumulator.with(expression, aggregated);
        }
        else {
            throw new MayflyException(
                expression.displayName() + 
                " is not aggregate or mentioned in GROUP BY"
            );
        }
        return accumulator;
    }

    private ResultRow addColumnsForKeys(
        GroupByCells cells, Selected selected, ResultRow accumulator) {
        if (keys.keyCount() != cells.size()) {
            throw new MayflyInternalException(
                "have " + keyColumns.size() + 
                " columns but " + cells.size() + " values");
        }
        for (int i = 0; i < cells.size(); ++i) {
            Cell cell = cells.get(i);
            Expression expression = (Expression) keyColumns.get(i);
            accumulator = accumulator.with(expression, cell);
        }
        return accumulator;
    }

}
