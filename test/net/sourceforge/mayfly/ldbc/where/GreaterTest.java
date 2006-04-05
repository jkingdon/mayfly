package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.parser.Parser;

public class GreaterTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new Greater(new SingleColumn("size"), new IntegerLiteral(6)),
                new Parser("size > 6").parseCondition().asBoolean()
        );
    }

    public void testEval() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", new Long(6))
                .appendColumnCellContents("colB", new Long(7))
        );

        assertFalse(new Greater(new IntegerLiteral(5), new SingleColumn("colA")).evaluate(row));
        assertFalse(new Greater(new IntegerLiteral(6), new SingleColumn("colA")).evaluate(row));
        assertTrue(new Greater(new IntegerLiteral(7), new SingleColumn("colA")).evaluate(row));
        assertTrue(new Greater(new SingleColumn("colB"), new SingleColumn("colA")).evaluate(row));
    }

}
