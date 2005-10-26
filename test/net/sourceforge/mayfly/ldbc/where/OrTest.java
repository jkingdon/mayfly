package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.ldbc.what.*;
import org.ldbc.parser.*;

public class OrTest extends TestCase {
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve' or species='homo sapiens' or size = 6");

        Tree orTree = selectTree.children()
                                .singleSubtreeOfType(SQLTokenTypes.CONDITION).children()
                                    .singleSubtreeOfType(SQLTokenTypes.LITERAL_or);

        assertEquals(
                new Or(
                    new Or(
                        new Equal(new Column("name"), new QuotedString("'steve'")),
                        new Equal(new Column("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Equal(new Column("size"), new MathematicalInt(6))
                ),
                Or.fromOrTree(orTree, TreeConverters.forSelectTree())
        );
    }

    public void testEval() throws Exception {
        assertTrue(new Or(new StringStartsWith("f"), new StringStartsWith("fo")).evaluate("foo"));
        assertTrue(new Or(new StringStartsWith("f"), new StringStartsWith("XX")).evaluate("foo"));
        assertTrue(new Or(new StringStartsWith("XX"), new StringStartsWith("fo")).evaluate("foo"));
        assertFalse(new Or(new StringStartsWith("XX"), new StringStartsWith("XX")).evaluate("foo"));
    }
}