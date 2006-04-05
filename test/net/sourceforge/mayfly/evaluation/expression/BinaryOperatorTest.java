package net.sourceforge.mayfly.evaluation.expression;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.where.literal.QuotedString;

public class BinaryOperatorTest extends TestCase {
    
    public void testSameExpression() throws Exception {
        Expression one = new Plus(new SingleColumn("x"), new IntegerLiteral(5));
        assertTrue(one.sameExpression(new Plus(new SingleColumn("X"), new IntegerLiteral(5))));
        
        assertFalse(one.sameExpression(new Plus(new SingleColumn("y"), new IntegerLiteral(5))));
        assertFalse(one.sameExpression(new Plus(new SingleColumn("x"), new IntegerLiteral(6))));
        assertFalse(one.sameExpression(new Minus(new SingleColumn("x"), new IntegerLiteral(5))));

        assertFalse(one.sameExpression(new QuotedString("'foo'")));
    }

}
