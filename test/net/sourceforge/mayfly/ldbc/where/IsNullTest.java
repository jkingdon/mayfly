package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.parser.*;

public class IsNullTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new IsNull(new SingleColumn("name")),
                new Parser("name is null").parseCondition()
        );
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
