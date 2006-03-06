package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.Rows;

abstract public class BinaryOperator extends Expression {

    private final Expression left;
    private final Expression right;

    public BinaryOperator(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public Cell evaluate(Row row) {
        Cell leftCell = left.evaluate(row);
        Cell rightCell = right.evaluate(row);
        return combine(leftCell, rightCell);
    }

    public Cell aggregate(Rows rows) {
        Cell leftCell = left.aggregate(rows);
        Cell rightCell = right.aggregate(rows);
        return combine(leftCell, rightCell);
    }
    
    public Cell findValue(int zeroBasedColumn, Row row) {
        if (firstAggregate() != null) {
            return row.byPosition(zeroBasedColumn);
        }
        else {
            return evaluate(row);
        }
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
    
    public void resolve(Row row) {
        left.resolve(row);
        right.resolve(row);
    }
    
    public Expression left() {
        return left;
    }
    
    public Expression right() {
        return right;
    }
    
}
