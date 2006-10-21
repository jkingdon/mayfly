package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.util.L;

import java.util.Iterator;

public class LeftJoin extends Join implements FromElement {

    public LeftJoin(FromElement left, FromElement right, Condition condition) {
        super(left, right, condition);
    }

    public ResultRows tableContents(DataStore store, String currentSchema) {
        ResultRows leftRows = left.tableContents(store, currentSchema);
        ResultRows rightRows = right.tableContents(store, currentSchema);

        final L joinResult = new L();

        Iterator leftIter = leftRows.iterator();
        while (leftIter.hasNext()) {
            ResultRow leftRow = (ResultRow) leftIter.next();
            boolean haveJoinedThisLeftRow = false;
            
            Iterator rightIter = rightRows.iterator();
            while (rightIter.hasNext()) {
                ResultRow rightRow = (ResultRow) rightIter.next();

                ResultRow combined = leftRow.combine(rightRow);
                
                if (condition.evaluate(combined)) {
                    joinResult.append(combined);
                    haveJoinedThisLeftRow = true;
                }
            }
            
            if (!haveJoinedThisLeftRow) {
                ResultRow nullRightRow = right.dummyRow(store, currentSchema);
                ResultRow withNulls = leftRow.combine(nullRightRow);
                joinResult.append(withNulls);
            }
        }
        return new ResultRows(joinResult.asImmutable());
    }

}
