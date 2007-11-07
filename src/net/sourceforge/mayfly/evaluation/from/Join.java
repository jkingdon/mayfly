package net.sourceforge.mayfly.evaluation.from;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

public abstract class Join extends FromElement {

    public final FromElement right;
    public final Condition condition;
    public final FromElement left;

    protected Join(FromElement left, FromElement right, Condition condition) {
        this.left = left;
        this.right = right;
        this.condition = condition;
    }

    @Override
    public ResultRow dummyRow(Evaluator evaluator) {
        ResultRow dummyRow = 
            left.dummyRow(evaluator)
                .combine(right.dummyRow(evaluator));
        condition.evaluate(dummyRow, evaluator);
        return dummyRow;
    }

}
