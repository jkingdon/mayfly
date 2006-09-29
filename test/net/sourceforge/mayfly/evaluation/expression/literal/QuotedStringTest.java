package net.sourceforge.mayfly.evaluation.expression.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Parser;

public class QuotedStringTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new QuotedString("'steve'"), 
            new Parser("'steve'").parsePrimary().asNonBoolean());
    }
    
    public void testValue() throws Exception {
        {
            StringCell cell = (StringCell) 
                new QuotedString("'steve'").evaluate((ResultRow)null);
            assertEquals("steve", cell.asString());
        }

        {
            StringCell cell = (StringCell) 
                new QuotedString("'a''''b'").evaluate((ResultRow)null);
            assertEquals("a''b", cell.asString());
        }
    }
    
    public void testSameExpression() throws Exception {
        assertTrue(new QuotedString("foo").sameExpression(
            new QuotedString("foo")));
        // Check that comparison is with equals, not ==
        assertTrue(
            new QuotedString("foo").sameExpression(
                new QuotedString(new String("foo"))));
    }

}
