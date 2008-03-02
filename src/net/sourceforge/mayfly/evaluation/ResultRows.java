package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
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
public class ResultRows implements Iterable<ResultRow> {

    private final ImmutableList<ResultRow> rows;

    public ResultRows(ResultRow singleRow) {
        this(ImmutableList.singleton(singleRow));
    }

    public ResultRows() {
        this(new ImmutableList());
    }
    
    public ResultRows(ImmutableList<ResultRow> rows) {
        this.rows = rows;
    }

    public int rowCount() {
        return rows.size();
    }

    public ResultRow row(int index) {
        return rows.get(index);
    }
    
    public Iterator<ResultRow> iterator() {
        return rows.iterator();
    }

    public ImmutableList asList() {
        return rows;
    }

    public ResultRows with(ResultRow row) {
        return new ResultRows(rows.with(row));
    }

    public ResultRows select(Condition condition) {
        return select(condition, Evaluator.NO_SUBSELECT_NEEDED);
    }

    public ResultRows select(Condition condition, Evaluator evaluator) {
        ResultRows selected = new ResultRows();
        for (ResultRow row : rows) {
            if (condition.evaluate(row, evaluator)) {
                selected = selected.with(row);
            }
        }
        return selected;
    }

    public ResultRows join(ResultRows right) {
        List result = new ArrayList();
        for (Iterator leftIterator = iterator(); leftIterator.hasNext();) {
            ResultRow leftRow = (ResultRow) leftIterator.next();
            for (Iterator rightIterator = right.iterator(); rightIterator.hasNext();) {
                ResultRow rightRow = (ResultRow) rightIterator.next();
                result.add(leftRow.combine(rightRow));
            }
        }
        return new ResultRows(new ImmutableList(result));
    }
    
    public ResultRow singleRow() {
        if (rowCount() != 1) {
            throw new MayflyInternalException("expected one row, got " + rowCount());
        }
        return row(0);
    }

    public String debugString() {
        StringBuilder out = new StringBuilder();
        out.append("row count = " + rowCount() + "\n");
        for (ResultRow row : rows) {
            out.append(row.debugString());
        }
        return out.toString();
    }

}
