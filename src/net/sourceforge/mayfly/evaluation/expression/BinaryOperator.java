package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;

abstract public class BinaryOperator extends Expression {

    protected final Expression left;
    protected final Expression right;

    protected BinaryOperator(Expression left, Expression right) {
        super(left.location.combine(right.location));
        this.left = left;
        this.right = right;
    }

    public Cell evaluate(ResultRow row) {
        Cell leftCell = left.evaluate(row);
        Cell rightCell = right.evaluate(row);
        return combine(leftCell, rightCell);
    }

    public Cell aggregate(ResultRows rows) {
        Cell leftCell = left.aggregate(rows);
        Cell rightCell = right.aggregate(rows);
        return combine(leftCell, rightCell);
    }
    
    abstract protected Cell combine(Cell left, Cell right);
    
    public String firstColumn() {
        String firstInLeft = left.firstColumn();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstColumn();
    }

    public String firstAggregate() {
        return Expression.firstAggregate(left, right);
    }

    public String displayName() {
        // Hard to get precedence and associativity right.
        // And we probably want to show the users's whitespace anyway(?).
        return "expression";
    }
    
    public boolean sameExpression(Expression other) {
        if (getClass().equals(other.getClass())) {
            BinaryOperator operator = (BinaryOperator) other;
            return left.sameExpression(operator.left) && right.sameExpression(operator.right);
        }
        else {
            return false;
        }
    }
    
    public abstract Expression resolve(ResultRow row);
    
    public void check(ResultRow row) {
        left.check(row);
        right.check(row);
    }

    public Expression left() {
        return left;
    }
    
    public Expression right() {
        return right;
    }
    
}
