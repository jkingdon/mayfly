package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.rowmask.*;

public class SelectTest extends TestCase {
    public void testParse() throws Exception {
        assertEquals(
            new Select(
                new RowMask()
                    .add(new WholeDimension("f"))
                    .add(new SingleColumnExpression(new Column("b", "name"))),
                new Dimensions()
                    .add(new Dimension("foo", "f"))
                    .add(new Dimension("bar", "b"))
            ),
            Select.fromTree(Tree.parse("select f.*, b.name from foo f, bar b"))
        );
    }
}