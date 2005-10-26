package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.parser.*;

public class NotEqTest extends TestCase {
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name <> 'steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree notEqualTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new NotEq(new Column("name"), new QuotedString("'steve'")),
                NotEq.fromNotEqualTree(notEqualTree, TreeConverters.forWhereTree())
        );
    }

    public void testParse2() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name != 'steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree notEqualTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new NotEq(new Column("name"), new QuotedString("'steve'")),
                NotEq.fromNotEqualTree(notEqualTree, TreeConverters.forWhereTree())
        );
    }

    public void testEval() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .asImmutable()
        );

        assertFalse(new NotEq(new Column("colA"), new QuotedString("'1'")).evaluate(row));
        assertTrue(new NotEq(new Column("colA"), new QuotedString("'2'")).evaluate(row));
    }
}