package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;
import org.ldbc.parser.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.*;

public class WhereTest extends TestCase {

    public void testEquals() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where f.name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);

        assertEquals(
            new Equal(new Column("f", "name"), new Literal.QuotedString("'steve'")),
            Equal.fromTree(new Tree(whereClause.getFirstChild()))
        );

    }

    public void testWhere() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where f.name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);

        assertEquals(
            new Where()
                .add(new Equal(new Column("f", "name"), new Literal.QuotedString("'steve'"))),
            Where.fromConditionTree(whereClause)
        );
    }


}