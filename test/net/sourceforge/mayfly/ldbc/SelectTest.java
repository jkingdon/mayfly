package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.rowmask.*;

public class SelectTest extends TestCase {
    public void testParse() throws Exception {
        assertEquals(
            new Select(
                new RowMask()
                    .add(new AllColumnsFromTable("f"))
                    .add(new SingleColumnExpression(new Column("b", "name"))),
                new Froms()
                    .add(new From("foo", "f"))
                    .add(new From("bar", "b")),
                new Where()
                    .add(new Where.Equal(new Column("f", "name"), new Literal.QuotedString("'steve'")))
            ),
            Select.fromTree(Tree.parse("select f.*, b.name from foo f, bar b where f.name='steve'"))
        );
    }
}