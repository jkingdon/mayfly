package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;
import net.sourceforge.mayfly.datastore.*;

public class QuotedStringTest extends TestCase {
    public void testMatchesCell() throws Exception {
        assertTrue(new QuotedString("'a'").matchesCell(new Cell("a")));
        assertFalse(new QuotedString("'b'").matchesCell(new Cell("a")));
    }
}