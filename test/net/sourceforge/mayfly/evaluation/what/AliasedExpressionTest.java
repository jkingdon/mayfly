package net.sourceforge.mayfly.evaluation.what;

import junit.framework.TestCase;

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

}
