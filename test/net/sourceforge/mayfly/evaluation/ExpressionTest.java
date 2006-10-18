package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.condition.Equal;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class ExpressionTest extends TestCase {

    public void testWhere() throws Exception {
        Equal where = (Equal) new Parser("f.name='steve'").parseWhere();
        MayflyAssert.assertColumn("f", "name", where.leftSide);
        MayflyAssert.assertString("steve", where.rightSide);
    }

    public void testSameExpression() throws Exception {
        Expression one = (Expression) new Parser("x + y * z / 2 || 5").parseWhatElement();
        Expression two = (Expression) new Parser("x+((y*z)/2) ||   5").parseWhatElement();
        assertTrue(one.sameExpression(two));
        assertTrue(two.sameExpression(one));
        
        Expression three = (Expression) new Parser("(x + y) * z / 2 || 5").parseWhatElement();
        assertFalse(three.sameExpression(one));
    }
    
    public void testResolve() throws Exception {
        Expression one = new Parser("x + 5 * avg(x) - count(*)").parseExpression().asNonBoolean();
        Expression resolved = one.resolveAndReturn(
            new Row(new TupleBuilder().appendColumnCell("foo", "x", NullCell.INSTANCE)));

        String expectedString = "foo.x + 5 * avg( foo.x ) - count ( * )";
        Expression expected = new Parser(expectedString).parseExpression().asNonBoolean();
        assertTrue(
            "expected " + expectedString + " but was:" + resolved.toString(), 
            expected.sameExpression(resolved));

        // And one should not be mutated
        assertFalse(
            "expected unchanged but was:" + one.toString(), 
            expected.sameExpression(one)
        );
    }
    
    public void testCheck() throws Exception {
        ResultRow row = new ResultRow()
            .withColumn("foo", "a", NullCell.INSTANCE)
            .withColumn("bar", "a", NullCell.INSTANCE);

        check("foo.a + bar.a", row);
        check("foo.a + bar.a * (bar.a / 5 - foo.a * bar.a)", row);

        try {
            check("baz.a", row);
            fail();
        }
        catch (NoColumn e) {
            assertEquals("no column baz.a", e.getMessage());
        }

        try {
            check("baz.a || 'hi'", row);
            fail();
        }
        catch (NoColumn e) {
            assertEquals("no column baz.a", e.getMessage());
        }

        try {
            check("foo.a + bar.a * (bar.a / 5 - foo.a * baz.a)", row);
            fail();
        }
        catch (NoColumn e) {
            assertEquals("no column baz.a", e.getMessage());
        }
    }

    private void check(String expressionString, ResultRow row) {
        Parser parser = new Parser(expressionString);
        Expression expression = (Expression) parser.parseWhatElement();
        assertEquals("", parser.remainingTokens());
        expression.check(row);
    }
    
}
