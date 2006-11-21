package net.sourceforge.mayfly.evaluation.expression.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class QuotedStringTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new QuotedString("'steve'"), 
            new Parser("'steve'").parsePrimary().asNonBoolean());
    }
    
    public void testValue() throws Exception {
        MayflyAssert.assertString("steve",
            new QuotedString("'steve'").evaluate((ResultRow)null));

        MayflyAssert.assertString("a''b",
            new QuotedString("'a''''b'").evaluate((ResultRow)null));
    }
    
    public void testSameExpression() throws Exception {
        assertTrue(new QuotedString("'foo'").sameExpression(
            new QuotedString("'foo'")));

        // Check that comparison is with equals, not ==
        assertTrue(
            new QuotedString("'foo'").sameExpression(
                new QuotedString(new String("'foo'"))));
    }

}
