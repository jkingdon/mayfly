package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.condition.Greater;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class GreaterTest extends TestCase {

    public void testParse() throws Exception {
        Greater greater = 
            (Greater) new Parser("size > 6").parseCondition().asBoolean();
        MayflyAssert.assertColumn("size", greater.leftSide);
        MayflyAssert.assertInteger(6, greater.rightSide);
    }

    public void testEval() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", 6)
                .appendColumnCellContents("colB", 7)
        );

        assertFalse(
            new Greater(new IntegerLiteral(5), new SingleColumn("colA"))
                .evaluate(row, "table1"));
        assertFalse(
            new Greater(new IntegerLiteral(6), new SingleColumn("colA"))
                .evaluate(row, "table1"));
        assertTrue(
            new Greater(new IntegerLiteral(7), new SingleColumn("colA"))
                .evaluate(row, "table1"));
        assertTrue(
            new Greater(new SingleColumn("colB"), new SingleColumn("colA"))
                .evaluate(row, "table1"));
    }

}
