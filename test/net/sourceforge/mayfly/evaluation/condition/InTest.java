package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.MayflyAssert;

public class InTest extends TestCase {
    
    public void testParse() throws Exception {
        In condition = (In) new Parser("a in (7, 10)").parseCondition().asBoolean();
            MayflyAssert.assertColumn("a", condition.leftSide);
            
            assertEquals(2, condition.expressions.size());
            assertEquals(7, ((IntegerLiteral)condition.expressions.get(0)).value);
            assertEquals(10, ((IntegerLiteral)condition.expressions.get(1)).value);
    }
    
    public void testEvaluate() throws Exception {
        In in = new In(
            new SingleColumn("a"),
            new L()
	            .append(new IntegerLiteral(1))
				.append(new IntegerLiteral(3)));
        assertFalse(in.evaluate(row(2), "table1"));
        assertTrue(in.evaluate(row(3), "table1"));
	}

    private Row row(long aValue) {
        return new TupleBuilder().append("a", aValue).asRow();
    }

}
