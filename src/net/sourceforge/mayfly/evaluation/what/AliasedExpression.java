package net.sourceforge.mayfly.evaluation.what;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;

/**
 * @internal
 * expression AS alias
 * 
 * (as in, for example, SELECT a + b AS TOTAL FROM foo).
 */
public class AliasedExpression extends Expression {

    public final String alias;
    private final Expression expression;

    public AliasedExpression(String aliasedColumn, Expression expression) {
        this.alias = aliasedColumn;
        this.expression = expression;
    }

    public String displayName() {
        return expression.displayName() + " AS " + alias;
    }

//    public Selected selected(Row dummyRow) {
//        throw new UnimplementedException();
//    }

    public Cell aggregate(ResultRows rows) {
        throw new UnimplementedException();
    }

    public Cell evaluate(ResultRow row) {
        return expression.evaluate(row);
    }

    public boolean sameExpression(Expression other) {
        throw new UnimplementedException();
    }
    
    public boolean matches(String columnName) {
        return alias.equalsIgnoreCase(columnName);
    }

}
