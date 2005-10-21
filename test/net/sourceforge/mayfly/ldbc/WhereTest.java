package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import org.ldbc.parser.*;
import net.sourceforge.mayfly.ldbc.rowmask.*;

public class WhereTest extends TestCase {

    public void testEquals() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where f.name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);

        assertEquals(
            new Where.Equal(new Column("f", "name"), new Literal.QuotedString("'steve'")),
            Where.Equal.fromTree(new Tree(whereClause.getFirstChild()))
        );

    }

    public void testWhere() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where f.name='steve'");

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);

        assertEquals(
            new Where()
                .add(new Where.Equal(new Column("f", "name"), new Literal.QuotedString("'steve'"))),
            Where.fromConditionTree(whereClause)
        );
    }


}