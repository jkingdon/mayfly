package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;
import org.ldbc.parser.*;

public class EqTest extends TestCase {

    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree equalTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new Eq(new Column("name"), new QuotedString("'steve'")),
                Eq.fromEqualTree(equalTree, TreeConverters.forWhereTree())
        );
    }

    public void testColumnAndQuotedString() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .asImmutable()
        );

        assertTrue(new Eq(new Column("colA"), new QuotedString("'1'")).evaluate(row));
        assertFalse(new Eq(new Column("colA"), new QuotedString("'2'")).evaluate(row));
    }
}