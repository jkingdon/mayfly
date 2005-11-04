package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

public class FromTest extends TestCase {
    public void testSimple() throws Exception {
        Tree tree = Tree.parse("select * from foo f, bar b, zzz");

        assertEquals(
            new From()
                .add(new FromElement("foo", "f"))
                .add(new FromElement("bar", "b"))
                .add(new FromElement("zzz")),
            From.fromSelectTree(tree)
        );
    }

}
