package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.GroupByCells;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.util.Iterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
   Selected is a list of expressions.  In the case of
   SELECT a, b, foo.* FROM . . .
   it is what we get after we expand the *'s, unlike
   {@link What} which is before that expansion.
   
   It also has been resolved in the sense of
   {@link Expression#resolve(ResultRow)}.
 */
public class Selected implements Iterable {

    private List expressions;

    public Selected() {
        this(new ArrayList());
    }

    public Selected(Expression singleExpression) {
        this(Collections.singletonList(singleExpression));
    }

    public Selected(List expressions) {
        this.expressions = expressions;
    }

    public Iterator iterator() {
        return expressions.iterator();
    }

    public Selected add(Expression expression) {
        expressions.add(expression);
        return this;
    }

    public Cell evaluate(int oneBasedColumn, ResultRow row) {
        int zeroBasedColumn = oneBasedColumn - 1;
        if (zeroBasedColumn < 0 || zeroBasedColumn >= expressions.size()) {
            throw new MayflyException("no column " + oneBasedColumn);
        }
        Expression element = (Expression) expressions.get(zeroBasedColumn);
        return row.findOrEvaluate(element);
    }

    public Cell evaluate(String columnName, ResultRow row) {
        Cell found = null;
        for (int i = 0; i < expressions.size(); ++i) {
            Expression expression = (Expression) expressions.get(i);
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
        for (int i = 0; i < expressions.size(); ++i) {
            Expression expression = (Expression) expressions.get(i);
            result = result.with(expression, expression.aggregate(rows));
        }
        return new ResultRows(result);
    }

    public int size() {
        return expressions.size();
    }

    public Expression element(int index) {
        return (Expression) expressions.get(index);
    }

    public GroupByCells evaluateAll(ResultRow row) {
        GroupByCells result = new GroupByCells();
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            Expression expression = (Expression) iter.next();
            result.add(expression.evaluate(row));
        }
        return result;
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
            result = result.with((Expression) expressions.get(i), cell);
        }
        return result;
    }

}
