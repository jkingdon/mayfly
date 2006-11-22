package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

/**
 * @internal
 * A case expression of the style
 * CASE
 *   { WHEN condition THEN result } ...
 *   ELSE result
 * END
 * 
 * as opposed to the "simple" case expression
 */
public class SearchedCase extends Expression {

    private final Expression elseExpression;
    private final ImmutableList conditions;
    private final ImmutableList thenValues;

    public SearchedCase() {
        this(new NullExpression(Location.UNKNOWN), Location.UNKNOWN,
            new ImmutableList(), new ImmutableList());
    }

    private SearchedCase(Expression elseExpression, Location location,
        ImmutableList conditions, ImmutableList thenValues) {
        super(location);
        this.elseExpression = elseExpression;
        this.conditions = conditions;
        this.thenValues = thenValues;
    }

    public Cell aggregate(ResultRows rows) {
        throw new UnimplementedException();
    }

    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        for (int i = 0; i < conditions.size(); ++i) {
            Condition condition = (Condition) conditions.get(i);
            if (condition.evaluate(row)) {
                Expression expression = (Expression) thenValues.get(i);
                return expression.evaluate(row);
            }
        }
        return elseExpression.evaluate(row);
    }

    public boolean sameExpression(Expression other) {
        throw new UnimplementedException();
    }

    public String displayName() {
        throw new UnimplementedException();
    }

    public SearchedCase withLocation(Location newLocation) {
        return new SearchedCase(elseExpression, newLocation, 
            conditions, thenValues);
    }

    public SearchedCase withElse(Expression elseExpression) {
        return new SearchedCase(elseExpression, location,
            conditions, thenValues);
    }
    
    public SearchedCase withCase(Condition condition, Expression thenValue) {
        return new SearchedCase(elseExpression, location,
            conditions.with(condition), thenValues.with(thenValue));
    }

}
