package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

import org.ldbc.parser.*;

public class NotEqTest extends TestCase {
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name <> 'steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree notEqualTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new Not(new Eq(new SingleColumnExpression("name"), new QuotedString("'steve'"))),
                NotEq.fromNotEqualTree(notEqualTree, TreeConverters.forWhereTree())
        );
    }

    public void testParse2() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name != 'steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree notEqualTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new Not(new Eq(new SingleColumnExpression("name"), new QuotedString("'steve'"))),
                NotEq.fromNotEqualTree(notEqualTree, TreeConverters.forWhereTree())
        );
    }

}