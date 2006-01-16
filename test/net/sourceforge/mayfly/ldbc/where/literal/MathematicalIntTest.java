package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.*;

import net.sourceforge.mayfly.parser.*;

public class MathematicalIntTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new MathematicalInt(5), new Parser("5").parsePrimary());
    }

    public void testValue() throws Exception {
        assertEquals(new Long(5), new MathematicalInt(5).valueForCellContentComparison());
    }

}
