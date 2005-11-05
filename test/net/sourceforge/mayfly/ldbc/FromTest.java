package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

public class FromTest extends TestCase {
    public void testSimple() throws Exception {
        Tree tree = Tree.parse("select * from foo f, bar b, zzz");

        assertEquals(
            new From()
                .add(new FromTable("foo", "f"))
                .add(new FromTable("bar", "b"))
                .add(new FromTable("zzz")),
            From.fromSelectTree(tree)
        );
    }

}
