package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.parser.Parser;

public class ExpressionTest extends TestCase {

    public void testSameExpression() throws Exception {
        Expression one = (Expression) new Parser("x + y * z / 2 || 5").parseWhatElement();
        Expression two = (Expression) new Parser("x+((y*z)/2) ||   5").parseWhatElement();
        assertTrue(one.sameExpression(two));
        assertTrue(two.sameExpression(one));
        
        Expression three = (Expression) new Parser("(x + y) * z / 2 || 5").parseWhatElement();
        assertFalse(three.sameExpression(one));
    }
    
}
