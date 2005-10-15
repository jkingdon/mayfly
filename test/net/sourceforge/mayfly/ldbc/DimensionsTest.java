package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import org.ldbc.parser.*;

public class DimensionsTest extends TestCase {
    public void testSimple() throws Exception {
        Tree tree = Tree.parse("select * from foo f, bar b, zzz");

        Iterable tables = tree.children().ofType(SQLTokenTypes.SELECTED_TABLE);

        assertEquals(
            new Dimensions()
                .add(new Dimension("foo", "f"))
                .add(new Dimension("bar", "b"))
                .add(new Dimension("zzz")),
            Dimensions.fromTableTrees(tables)
        );
    }

    /**
     * Ideas.
     *
     * dimensions.join(allTables) : Rows (cartesian join result)
     *
     * then you'd do
     *
     * rows = rows.select(where)
     *
     * rows = rows.collect(new ColumnFilter(colsFromSelect))
     *
     * rows is a stream only.
     *
     *
     * consequence: change enumerable to ALWAYS stream (createNew(Iterable))
     */
}