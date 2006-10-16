package net.sourceforge.mayfly.evaluation.from;

import junit.framework.TestCase;

import net.sourceforge.mayfly.parser.Parser;

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
    
    public void xtestDuplicate() throws Exception {
        // I think I'm going to fix this another way,
        // by looking for duplicates as we build the
        // dummy row.
        /*
        From from = new From()
            .add(new InnerJoin(
                new FromTable("foo", "t"),
                new InnerJoin(
                    new FromTable("bar"),
                    new FromTable("baz", "T"),
                    BooleanExpression.TRUE
                ),
                BooleanExpression.TRUE
            ));
        try {
            from.check();
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate table name or alias t", e.getMessage());
        }
        */
    }
    
}
