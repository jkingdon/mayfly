package net.sourceforge.mayfly.evaluation.expression.literal;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class IntegerLiteralTest extends TestCase {

    public void testParse() throws Exception {
        MayflyAssert.assertInteger(5, new Parser("5").parsePrimary().asNonBoolean());
    }

    public void testValue() throws Exception {
        LongCell cell = (LongCell) new IntegerLiteral(5).evaluate((ResultRow)null);
        assertEquals(5L, cell.asLong());
    }

}
