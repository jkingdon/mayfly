package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.parser.*;

public class QuotedStringTest extends TestCase {
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve'");

        Tree quotedStringTree =
            selectTree.children()
                .singleSubtreeOfType(SQLTokenTypes.CONDITION).children()
                    .singleSubtreeOfType(SQLTokenTypes.EQUAL).children()
                        .singleSubtreeOfType(SQLTokenTypes.QUOTED_STRING);

        assertEquals(new QuotedString("'steve'"), QuotedString.fromQuotedStringTree(quotedStringTree));
    }

    public void testMatchesCell() throws Exception {
        assertTrue(new QuotedString("'a'").matchesCell(new Cell("a")));
        assertFalse(new QuotedString("'b'").matchesCell(new Cell("a")));

        assertTrue(new QuotedString("'don''t'").matchesCell(new Cell("don't")));
        assertFalse(new QuotedString("'don''t'").matchesCell(new Cell("don''t")));
    }

    public void testValue() throws Exception {
        assertEquals("steve", new QuotedString("'steve'").valueForCellContentComparison());
        assertEquals("a''b", new QuotedString("'a''''b'").valueForCellContentComparison());
    }

}
