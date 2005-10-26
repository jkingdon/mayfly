package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;
import org.ldbc.parser.*;

public class GtTest extends TestCase {
    public void testParse() throws Exception {
        Tree gtTree = Tree.parse("select * from foo where size > 6")
                            .children().singleSubtreeOfType(SQLTokenTypes.CONDITION)
                                .children().singleSubtreeOfType(SQLTokenTypes.BIGGER);

        assertEquals(
                new Gt(new Column("size"), new MathematicalInt(6)),
                Gt.fromBiggerTree(gtTree, TreeConverters.forWhereTree())
        );
    }

    public void testEval() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell(new Long(6)))
                .entry(new Column("colB"), new Cell(new Long(7)))
                .asImmutable()
        );

        assertFalse(new Gt(new MathematicalInt(5), new Column("colA")).evaluate(row));
        assertFalse(new Gt(new MathematicalInt(6), new Column("colA")).evaluate(row));
        assertTrue(new Gt(new MathematicalInt(7), new Column("colA")).evaluate(row));
        assertTrue(new Gt(new Column("colB"), new Column("colA")).evaluate(row));
    }
}