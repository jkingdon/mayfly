package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.ScalarSubselect;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.select.Select;

public class SubselectedIn extends Condition {

    private final Expression leftSide;
    private final Select subselect;

    public SubselectedIn(Expression leftSide, Select subselect) {
        this.leftSide = leftSide;
        this.subselect = subselect;
    }

    @Override
    public boolean evaluate(ResultRow row, Evaluator evaluator) {
        Cell leftCell = leftSide.evaluate(row, evaluator);
        MayflyResultSet rows = 
            ScalarSubselect.subselect(row, evaluator, subselect);
        while (rows.next()) {
            Cell aRightSideValue = rows.singleColumn(subselect.location);
            if (leftCell.sqlEquals(aRightSideValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Condition resolve(ResultRow row, Evaluator evaluator) {
        Expression newLeftSide = leftSide.resolve(row, evaluator);
        // Probably should be resolving the subselect too.
        if (newLeftSide != leftSide) {
            return new SubselectedIn(leftSide, subselect);
        }
        else {
            return this;
        }
    }

    @Override
    public String firstAggregate() {
        return leftSide.firstAggregate();
    }

    @Override
    public void check(ResultRow row) {
        leftSide.check(row);
        throw NoColumn.dummyExceptionForSubselect(subselect.location);
    }

}
