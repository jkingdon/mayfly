package net.sourceforge.mayfly.evaluation.expression.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.ResultRow;

public class LiteralTest extends TestCase {

    public void testTransform() throws Exception {
        assertEquals(
            new StringCell("foo"),
            new QuotedString("'foo'").evaluate(new ResultRow())
        );
    }
    
    public void testSameExpression() throws Exception {
        assertTrue(new IntegerLiteral(77).sameExpression(new IntegerLiteral(77)));
        assertFalse(new IntegerLiteral(78).sameExpression(new IntegerLiteral(77)));
        assertFalse(new QuotedString("'77'").sameExpression(new IntegerLiteral(77)));
        assertFalse(new IntegerLiteral(77).sameExpression(new QuotedString("'77'")));
        
        assertTrue(new QuotedString("'foo'").sameExpression(new QuotedString("'foo'")));
        assertFalse(new QuotedString("'foo'").sameExpression(new QuotedString("'food'")));
    }
    
    public void testBigDecimalAndSame() throws Exception {
        assertTrue(new DecimalLiteral("7.0").sameExpression(new DecimalLiteral("7.0")));
        assertFalse(new DecimalLiteral("7.00").sameExpression(new DecimalLiteral("7.0")));
        assertFalse(new DecimalLiteral("7.00").sameExpression(new IntegerLiteral(7)));
        assertFalse(new IntegerLiteral(7).sameExpression(new DecimalLiteral("7.00")));
    }

}
