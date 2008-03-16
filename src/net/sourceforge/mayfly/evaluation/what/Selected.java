package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.GroupByCells;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
   @internal
   Selected is a list of expressions.
   
   In the case of
   SELECT a, b, foo.* FROM . . .
   it is what we get after we expand the *'s, unlike
   {@link What} which is before that expansion.
   
   It also has been resolved in the sense of
   {@link Expression#resolve(ResultRow, Evaluator)}.
 */
public class Selected implements Iterable<Expression> {

    private final ImmutableList<Expression> expressions;

    public Selected() {
        this(new ImmutableList<Expression>());
    }

    public Selected(Expression... expressions) {
        this(ImmutableList.fromArray(expressions));
    }

    public Selected(ImmutableList<Expression> expressions) {
        this.expressions = expressions;
    }

    public Iterator<Expression> iterator() {
        return expressions.iterator();
    }

    public Cell evaluate(int oneBasedColumn, ResultRow row) {
        int zeroBasedColumn = oneBasedColumn - 1;
        if (zeroBasedColumn < 0 || zeroBasedColumn >= expressions.size()) {
            throw new MayflyException("no column " + oneBasedColumn);
        }
        Expression element = expressions.get(zeroBasedColumn);
        return row.findOrEvaluate(element);
    }

    public Cell evaluate(String columnName, ResultRow row) {
        Cell found = null;
        for (Expression expression : expressions) {
            if (expression.matches(columnName)) {
                if (found != null) {
                    throw new MayflyException("ambiguous column " + columnName);
                }
                else {
                    found = expression.evaluate(row);
                }
            }
        }

        if (found == null) {
            throw new NoColumn(columnName);
        }
        else {
            return found;
        }
    }

    public ResultRows aggregate(ResultRows rows) {
        ResultRow result = new ResultRow();
        for (Expression expression : expressions) {
            result = result.with(expression, expression.aggregate(rows));
        }
        return new ResultRows(result);
    }

    public int size() {
        return expressions.size();
    }

    public Expression element(int index) {
        return expressions.get(index);
    }

    public GroupByCells evaluateAll(ResultRow row) {
        List<Cell> result = new ArrayList<Cell>();
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            Expression expression = (Expression) iter.next();
            result.add(expression.evaluate(row));
        }
        return new GroupByCells(result);
    }

    public ResultRow toRow(GroupByCells cells) {
        if (expressions.size() != cells.size()) {
            throw new MayflyInternalException(
                "matching " + expressions.size() + 
                " expressions with " + cells.size() + " cells");
        }

        ResultRow result = new ResultRow();
        for (int i = 0; i < cells.size(); ++i) {
            Cell cell = cells.get(i);
            result = result.with(expressions.get(i), cell);
        }
        return result;
    }

}
