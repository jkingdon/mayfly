package net.sourceforge.mayfly.evaluation.expression.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.parser.Parser;

public class IntegerLiteralTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(new IntegerLiteral(5), new Parser("5").parsePrimary().asNonBoolean());
    }

    public void testValue() throws Exception {
        LongCell cell = (LongCell) new IntegerLiteral(5).evaluate(null);
        assertEquals(5L, cell.asLong());
    }

}
