package net.sourceforge.mayfly.evaluation.from;

import junit.framework.TestCase;

import net.sourceforge.mayfly.parser.Parser;

public class FromTest extends TestCase {

    public void testSimple() throws Exception {
        From from = new Parser("foo f, bar b, zzz").parseFromItems();
        
        assertEquals(3, from.size());
        checkElement("foo", "f", from, 0);
        checkElement("bar", "b", from, 1);
        checkElement("zzz", "zzz", from, 2);
    }

    private void checkElement(String expectedTable, String expectedAlias, 
        From from, int index) {
        FromTable table = (FromTable) from.element(index);
        assertEquals(expectedTable, table.tableName);
        assertEquals(expectedAlias, table.alias);
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
