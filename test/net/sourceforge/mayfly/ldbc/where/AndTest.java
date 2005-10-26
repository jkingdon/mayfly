package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import org.ldbc.parser.*;

public class AndTest extends TestCase {
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve' and species='homo sapiens' and size = 6");

        Tree andTree = selectTree.children()
                                .singleSubtreeOfType(SQLTokenTypes.CONDITION).children()
                                    .singleSubtreeOfType(SQLTokenTypes.LITERAL_and);

        assertEquals(
                new And(
                    new And(
                        new Equal(new Column("name"), new QuotedString("'steve'")),
                        new Equal(new Column("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Equal(new Column("size"), new MathematicalInt(6))
                ),
            And.fromAndTree(andTree, TreeConverters.forSelectTree())
        );
    }

    public void testEval() throws Exception {
        assertTrue(new And(new StringStartsWith("f"), new StringStartsWith("fo")).evaluate("foo"));
        assertFalse(new And(new StringStartsWith("f"), new StringStartsWith("XX")).evaluate("foo"));
        assertFalse(new And(new StringStartsWith("XX"), new StringStartsWith("fo")).evaluate("foo"));
        assertFalse(new And(new StringStartsWith("XX"), new StringStartsWith("XX")).evaluate("foo"));
    }

}