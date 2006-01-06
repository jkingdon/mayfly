package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

public class GreaterTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new Greater(new SingleColumn("size"), new MathematicalInt(6)),
                new Parser("size > 6").parseCondition()
        );
    }

    public void testEval() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", new Long(6))
                .appendColumnCellContents("colB", new Long(7))
        );

        assertFalse(new Greater(new MathematicalInt(5), new SingleColumn("colA")).evaluate(row));
        assertFalse(new Greater(new MathematicalInt(6), new SingleColumn("colA")).evaluate(row));
        assertTrue(new Greater(new MathematicalInt(7), new SingleColumn("colA")).evaluate(row));
        assertTrue(new Greater(new SingleColumn("colB"), new SingleColumn("colA")).evaluate(row));
    }

}
