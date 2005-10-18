package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import org.ldbc.parser.*;
import net.sourceforge.mayfly.ldbc.rowmask.*;

public class ConstraintTest extends TestCase {

    public void testBlah() throws Exception {

    }

    public void xtestEquals() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where f.name='steve'");

        //selectTree.print();

        Tree whereClause = selectTree.children().singleSubtreeOfType(SQLTokenTypes.CONDITION);


        assertEquals(
            new Constraint.Equal(new Column("f", "name"), new Literal.QuotedString("'steve'")),
            Constraint.fromTree(new Tree(whereClause.getFirstChild()))
        );

    }
}