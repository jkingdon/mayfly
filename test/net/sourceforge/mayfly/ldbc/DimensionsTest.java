package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import org.ldbc.parser.*;

public class DimensionsTest extends TestCase {
    public void testSimple() throws Exception {
        Tree tree = Tree.parse("select * from foo f, bar b, zzz");

        Iterable<Tree> tables = tree.children().ofType(SQLTokenTypes.SELECTED_TABLE);

        assertEquals(
            new Dimensions(
                new Dimension("foo", "f"),
                new Dimension("bar", "b"),
                new Dimension("zzz")
            ),
            Dimensions.fromTableTrees(tables)
        );

    }
}