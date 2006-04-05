package net.sourceforge.mayfly.evaluation.expression.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.parser.Parser;

public class IntegerLiteralTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new IntegerLiteral(5), new Parser("5").parsePrimary().asNonBoolean());
    }

    public void testValue() throws Exception {
        assertEquals(new Long(5), new IntegerLiteral(5).valueForCellContentComparison());
    }

}
