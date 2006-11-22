package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.util.L;

import java.util.Iterator;

public class LeftJoin extends Join {

    public LeftJoin(FromElement left, FromElement right, Condition condition) {
        super(left, right, condition);
    }

    public ResultRows tableContents(Evaluator evaluator) {
        ResultRows leftRows = left.tableContents(evaluator);
        ResultRows rightRows = right.tableContents(evaluator);

        final L joinResult = new L();

        Iterator leftIter = leftRows.iterator();
        while (leftIter.hasNext()) {
            ResultRow leftRow = (ResultRow) leftIter.next();
            boolean haveJoinedThisLeftRow = false;
            
            Iterator rightIter = rightRows.iterator();
            while (rightIter.hasNext()) {
                ResultRow rightRow = (ResultRow) rightIter.next();

                ResultRow combined = leftRow.combine(rightRow);
                
                if (condition.evaluate(combined, evaluator)) {
                    joinResult.append(combined);
                    haveJoinedThisLeftRow = true;
                }
            }
            
            if (!haveJoinedThisLeftRow) {
                ResultRow nullRightRow = right.dummyRow(evaluator);
                ResultRow withNulls = leftRow.combine(nullRightRow);
                joinResult.append(withNulls);
            }
        }
        return new ResultRows(joinResult.asImmutable());
    }

}
