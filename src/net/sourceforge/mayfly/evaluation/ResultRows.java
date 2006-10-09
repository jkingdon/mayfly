package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @internal
 * Rows that we might return as part of evaluating.
 * That is, they may have aliases applied, or
 * various things other than just a cell in
 * each column.
 */
public class ResultRows {

    private final ImmutableList rows;

    public ResultRows(Rows rows) {
        this(makeResultRows(rows));
    }

    private static ImmutableList makeResultRows(Rows inputRows) {
        List result = new ArrayList();
        for (Iterator iter = inputRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            result.add(new ResultRow(row));
        }
        return new ImmutableList(result);
    }
    
    public ResultRows(ResultRow singleRow) {
        this(ImmutableList.singleton(singleRow));
    }

    public ResultRows() {
        this(new ImmutableList());
    }
    
    public ResultRows(ImmutableList rows) {
        this.rows = rows;
    }

    public int size() {
        return rows.size();
    }

    public ResultRow row(int index) {
        return (ResultRow) rows.get(index);
    }
    
    public Iterator iterator() {
        return rows.iterator();
    }

    public ImmutableList asList() {
        return rows;
    }

    public ResultRows with(ResultRow row) {
        return new ResultRows(rows.with(row));
    }

    public ResultRows select(BooleanExpression condition) {
        ResultRows selected = new ResultRows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ResultRow row = (ResultRow) iter.next();
            if (condition.evaluate(row)) {
                selected = selected.with(row);
            }
        }
        return selected;
    }

}
