package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class IsNullTest extends TestCase {

    public void testParse() throws Exception {
        IsNull condition = (IsNull) new Parser("name is null").parseCondition().asBoolean();
        MayflyAssert.assertColumn("name", condition.expression);
    }

    public void testEvaluate() throws Exception {
        Row nullRow = new Row(
            new TupleBuilder()
                .appendColumnCell("colA", NullCell.INSTANCE)
        );

        assertTrue(new IsNull(new SingleColumn("colA")).evaluate(nullRow));

        Row nonNullRow = new Row(
            new TupleBuilder()
                .appendColumnCell("colA", new StringCell("foo"))
        );

        assertFalse(new IsNull(new SingleColumn("colA")).evaluate(nonNullRow));
    }

}
