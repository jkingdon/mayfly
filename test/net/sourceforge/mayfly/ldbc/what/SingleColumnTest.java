package net.sourceforge.mayfly.ldbc.what;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.ldbc.where.literal.MathematicalInt;
import net.sourceforge.mayfly.parser.Parser;

public class SingleColumnTest extends TestCase {

    public void testRowTransform() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
        );

        assertEquals(new StringCell("1"), new SingleColumn("colA").evaluate(row));
        assertEquals(new StringCell("2"), new SingleColumn("colB").evaluate(row));
    }

    public void testSameColumn() throws Exception {
        Expression one = (Expression) new Parser("foo.x").parseWhatElement();
        Expression two = (Expression) new Parser("Foo . X").parseWhatElement();

        assertTrue(one.sameExpression(two));
        assertTrue(two.sameExpression(one));

        assertFalse(new SingleColumn("x").sameExpression(one));
        
        assertFalse(new SingleColumn("x").sameExpression(new MathematicalInt(5)));
        
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
