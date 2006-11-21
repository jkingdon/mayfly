package net.sourceforge.mayfly.evaluation.expression;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class SingleColumnTest extends TestCase {

    public void testRowTransform() throws Exception {
        ResultRow row = new ResultRow()
            .withColumn("t", "colA", "1")
            .withColumn("t", "colB", "2")
        ;

        MayflyAssert.assertString("1", new SingleColumn("colA").evaluate(row));
        MayflyAssert.assertString("2", new SingleColumn("colB").evaluate(row));
    }

    public void testSameColumn() throws Exception {
        Expression one = (Expression) new Parser("foo.x").parseWhatElement();
        Expression two = (Expression) new Parser("Foo . X").parseWhatElement();

        assertTrue(one.sameExpression(two));
        assertTrue(two.sameExpression(one));

        assertFalse(new SingleColumn("x").sameExpression(one));
        
        assertFalse(new SingleColumn("x").sameExpression(new IntegerLiteral(5)));
        
        assertFalse(one.sameExpression(new SingleColumn("foo", "y")));
     }
     
    public void testPossiblyNullEquals() throws Exception {
        assertTrue(SingleColumn.possiblyNullEquals("x", "X"));
        assertFalse(SingleColumn.possiblyNullEquals("x", "xy"));
        assertFalse(SingleColumn.possiblyNullEquals("x", null));
        assertFalse(SingleColumn.possiblyNullEquals(null, "X"));
        assertTrue(SingleColumn.possiblyNullEquals(null, null));
    }

}
