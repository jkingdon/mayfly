package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.parser.*;

public class QuotedStringTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new QuotedString("'steve'"), new Parser("'steve'").parsePrimary());
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
