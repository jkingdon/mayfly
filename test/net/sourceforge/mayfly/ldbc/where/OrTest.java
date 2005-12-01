package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class OrTest extends TestCase {
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve' or species='homo sapiens' or size = 6");

        Tree orTree = selectTree.children()
                                .singleSubtreeOfType(SQLTokenTypes.CONDITION).children()
                                    .singleSubtreeOfType(SQLTokenTypes.LITERAL_or);

        assertEquals(
                new Or(
                    new Or(
                        new Eq(new SingleColumn("name"), new QuotedString("'steve'")),
                        new Eq(new SingleColumn("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Eq(new SingleColumn("size"), new MathematicalInt(6))
                ),
                Or.fromOrTree(orTree, TreeConverters.forWhereTree())
        );
    }

    public void testEvaluate() throws Exception {
        assertTrue(new Or(new StringStartsWith("f"), new StringStartsWith("fo")).evaluate("foo"));
        assertTrue(new Or(new StringStartsWith("f"), new StringStartsWith("XX")).evaluate("foo"));
        assertTrue(new Or(new StringStartsWith("XX"), new StringStartsWith("fo")).evaluate("foo"));
        assertFalse(new Or(new StringStartsWith("XX"), new StringStartsWith("XX")).evaluate("foo"));
    }
}