package net.sourceforge.mayfly.evaluation.expression;

import junit.framework.TestCase;

import net.sourceforge.mayfly.parser.Location;

public class AggregateExpressionTest extends TestCase {
    
    public void testSameExpression() throws Exception {
        Maximum one = new Maximum(new SingleColumn("x"), "MAX", false);
        Maximum two = new Maximum(new SingleColumn("X"), "Max", false);
        assertTrue(one.sameExpression(two));
        assertTrue(one.sameExpression(
            new Maximum(new SingleColumn("x"), "MAX", false, new Location (5, 6, 7, 8))
        ));
        
        assertFalse(one.sameExpression(new Maximum(new SingleColumn("y"), "MAX", false)));
        assertFalse(one.sameExpression(new Maximum(new SingleColumn("x"), "MAX", true)));
        assertFalse(one.sameExpression(new Minimum(new SingleColumn("x"), "MAX", false)));

        assertFalse(one.sameExpression(new CountAll("count")));
        assertFalse(new CountAll("count").sameExpression(one));
        assertTrue(new CountAll("COUNT").sameExpression(new CountAll("count")));
    }

}
