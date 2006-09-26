package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.CellHeader;
import net.sourceforge.mayfly.evaluation.Expression;

/**
 * @internal
 * This class is just a transitional aid at this point.
 *
 * A ResultRow is a map from expression to cell.
 * A Row is a map from header (which can be a PositionalHeader)
 * to cell.
 * 
 * So a Row which is using a PositionalHeader really
 * wants to be a ResultRow.
 */
public class PositionalHeader implements CellHeader {

    public final Expression expression;

    public PositionalHeader(Expression expression) {
        this.expression = expression;
    }
    
}
