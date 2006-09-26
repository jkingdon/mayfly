package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.what.What;

public class ColumnOrderItem extends OrderItem {

    private final SingleColumn column;

    public ColumnOrderItem(SingleColumn column, boolean ascending) {
        super(ascending);
        this.column = column;
    }
    
    protected int compareAscending(What what, ResultRow first, ResultRow second) {
        return compare(first, second, column);
    }

    public static int compare(ResultRow first, ResultRow second, SingleColumn column) {
        Cell cell1 = column.evaluate(first);
        Cell cell2 = column.evaluate(second);
        return cell1.compareTo(cell2);
    }
    
    public void check(Row dummyRow) {
        column.evaluate(dummyRow);
    }
    
}
