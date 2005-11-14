package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import org.ldbc.parser.*;

public class GtTest extends TestCase {
    public void testParse() throws Exception {
        Tree gtTree = Tree.parse("select * from foo where size > 6")
                            .children().singleSubtreeOfType(SQLTokenTypes.CONDITION)
                                .children().singleSubtreeOfType(SQLTokenTypes.BIGGER);

        assertEquals(
                new Gt(new SingleColumn("size"), new MathematicalInt(6)),
                Gt.fromBiggerTree(gtTree, TreeConverters.forWhereTree())
        );
    }

    public void testEval() throws Exception {
        Row row = new Row(
            new Tuples()
                .appendColumnCellTuple("colA", new Long(6))
                .appendColumnCellTuple("colB", new Long(7))
        );

        assertFalse(new Gt(new MathematicalInt(5), new SingleColumn("colA")).evaluate(row));
        assertFalse(new Gt(new MathematicalInt(6), new SingleColumn("colA")).evaluate(row));
        assertTrue(new Gt(new MathematicalInt(7), new SingleColumn("colA")).evaluate(row));
        assertTrue(new Gt(new SingleColumn("colB"), new SingleColumn("colA")).evaluate(row));
    }
}