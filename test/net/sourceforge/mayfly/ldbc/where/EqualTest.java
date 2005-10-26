package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;
import org.ldbc.parser.*;

public class EqualTest extends TestCase {

    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);
        Tree equalTree = new Tree(whereClause.getFirstChild());

        assertEquals(
                new Equal(new Column("name"), new QuotedString("'steve'")),
                Equal.fromEqualTree(equalTree, TreeConverters.forSelectTree())
        );
    }

    public void testColumnAndQuotedString() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .entry(new Column("colB"), new Cell("2"))
                .asImmutable()
        );

        assertTrue(new Equal(new Column("colA"), new QuotedString("'1'")).evaluate(row));
        assertFalse(new Equal(new Column("colA"), new QuotedString("'2'")).evaluate(row));
    }
}