package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.parser.Parser;

public class MathematicalIntTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new MathematicalInt(5), new Parser("5").parsePrimary().asNonBoolean());
    }

    public void testValue() throws Exception {
        assertEquals(new Long(5), new MathematicalInt(5).valueForCellContentComparison());
    }

}
