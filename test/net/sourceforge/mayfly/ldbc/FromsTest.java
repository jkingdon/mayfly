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

    public void testTableNames() throws Exception {
        assertEquals("foo", new Froms.GetTableName().transform(new From("foo")));
        assertEquals("foo", new Froms.GetTableName().transform(new From("foo", "f")));
    }
}