package net.sourceforge.mayfly.evaluation;

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
public class ResultRows {

    private final ImmutableList rows;

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

    public ResultRows select(Condition condition) {
        return select(condition, Evaluator.NO_SUBSELECT_NEEDED);
    }

    public ResultRows select(Condition condition, Evaluator evaluator) {
        ResultRows selected = new ResultRows();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ResultRow row = (ResultRow) iter.next();
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

}
