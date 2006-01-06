package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.parser.*;

public class FromTest extends TestCase {
    public void testSimple() throws Exception {
        assertEquals(
            new From()
                .add(new FromTable("foo", "f"))
                .add(new FromTable("bar", "b"))
                .add(new FromTable("zzz")),
            new Parser("foo f, bar b, zzz").parseFromItems()
        );
    }

}
