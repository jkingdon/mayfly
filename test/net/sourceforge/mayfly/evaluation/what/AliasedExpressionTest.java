package net.sourceforge.mayfly.evaluation.what;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.Sum;

public class AliasedExpressionTest extends TestCase {
    
    public void testDisplayName() throws Exception {
        AliasedExpression element = new AliasedExpression(
            "total", 
            new Sum(new SingleColumn("x"), "Sum", false)
        );
        assertEquals("Sum(x) AS total", element.displayName());
    }

    public void testAggregate() throws Exception {
        AliasedExpression element = new AliasedExpression(
            "total", 
            new Sum(new SingleColumn("x"), "Sum", false)
        );
        assertEquals("Sum(x)", element.firstAggregate());
        assertEquals(null, element.firstColumn());
    }

    public void testNonAggregate() throws Exception {
        AliasedExpression element = new AliasedExpression(
            "total", 
            new Plus(new SingleColumn("x"), new SingleColumn("y"))
        );
        assertEquals(null, element.firstAggregate());
        assertEquals("x", element.firstColumn());
    }
    
    public void testSameExpression() throws Exception {
        AliasedExpression one = new AliasedExpression(
            "total",
            new SingleColumn("all_of_it")
        );
        AliasedExpression justLikeOne = new AliasedExpression(
            "total",
            new SingleColumn("all_of_it")
        );
        AliasedExpression differentAlias = new AliasedExpression(
            "whole_enchilada",
            new SingleColumn("all_of_it")
        );
        AliasedExpression differentExpression = new AliasedExpression(
            "total",
            new SingleColumn("x")
        );
        
        assertTrue(one.sameExpression(justLikeOne));
        assertFalse(one.sameExpression(differentAlias));
        assertFalse(one.sameExpression(differentExpression));
        assertFalse(one.sameExpression(new SingleColumn("non_alias")));
    }
    
    // resolve

}
