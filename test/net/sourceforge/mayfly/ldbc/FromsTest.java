package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

public class FromsTest extends TestCase {
    public void testSimple() throws Exception {
        Tree tree = Tree.parse("select * from foo f, bar b, zzz");

        assertEquals(
            new Froms()
                .add(new From("foo", "f"))
                .add(new From("bar", "b"))
                .add(new From("zzz")),
            Froms.fromSelectTree(tree)
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