package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import org.ldbc.parser.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

public class NotTest extends TestCase {
    
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where not name = 'jim'");
        Tree notTree = selectTree.children()
            .singleSubtreeOfType(SQLTokenTypes.CONDITION).children()
                .singleSubtreeOfType(SQLTokenTypes.NOT);
        assertEquals(
            new Not(new Eq(new Column("name"), new QuotedString("'jim'"))),
            Not.fromNotTree(notTree, TreeConverters.forWhereTree())
        );
    }
    
    public void testEvaluate() throws Exception {
        assertTrue(new Not(new StringStartsWith("f")).evaluate("bar"));
        assertFalse(new Not(new StringStartsWith("f")).evaluate("foo"));
    }

}
