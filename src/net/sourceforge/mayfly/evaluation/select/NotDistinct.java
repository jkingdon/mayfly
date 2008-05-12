package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.what.Selected;

public class NotDistinct extends Distinct {

    @Override
    public ResultRows distinct(Selected selected, ResultRows rows) {
        return rows;
    }

}
