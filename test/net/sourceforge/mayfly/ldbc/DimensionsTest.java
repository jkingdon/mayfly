package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

public class DimensionsTest extends TestCase {
    public void testSimple() throws Exception {
        Tree tree = Tree.parse("select * from foo f, bar b, zzz");

        assertEquals(
            new Dimensions()
                .add(new Dimension("foo", "f"))
                .add(new Dimension("bar", "b"))
                .add(new Dimension("zzz")),
            Dimensions.fromSelectTree(tree)
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