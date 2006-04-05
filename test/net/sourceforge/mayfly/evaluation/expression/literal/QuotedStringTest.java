package net.sourceforge.mayfly.evaluation.expression.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.parser.Parser;

public class QuotedStringTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new QuotedString("'steve'"), new Parser("'steve'").parsePrimary().asNonBoolean());
    }

    public void testMatchesCell() throws Exception {
        assertTrue(new QuotedString("'a'").matchesCell(new StringCell("a")));
        assertFalse(new QuotedString("'b'").matchesCell(new StringCell("a")));

        assertTrue(new QuotedString("'don''t'").matchesCell(new StringCell("don't")));
        assertFalse(new QuotedString("'don''t'").matchesCell(new StringCell("don''t")));
    }

    public void testValue() throws Exception {
        assertEquals("steve", new QuotedString("'steve'").valueForCellContentComparison());
        assertEquals("a''b", new QuotedString("'a''''b'").valueForCellContentComparison());
    }

}
