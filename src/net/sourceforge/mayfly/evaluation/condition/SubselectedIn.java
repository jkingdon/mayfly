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

    private final Select subselect;
    private final Expression leftSide;

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
    public String firstAggregate() {
        return leftSide.firstAggregate();
    }

    @Override
    public void check(ResultRow row) {
        leftSide.check(row);
        throw NoColumn.dummyExceptionForSubselect(subselect.location);
    }

}
