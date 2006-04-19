package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.util.ImmutableList;

public class InsertTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                ImmutableList.fromArray(new String[] {"a", "b"}),
                ImmutableList.fromArray(new Object[] {new LongCell(5), new StringCell("Value")})
            ),
            Command.fromSql("insert into foo (a, b) values (5, 'Value')")
        );
    }
    
    public void testParseNull() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                ImmutableList.fromArray(new String[] {"a"}),
                ImmutableList.fromArray(new Object[] {NullCell.INSTANCE})
            ),
            Command.fromSql("insert into foo (a) values (null)")
        );
    }
    
    public void testParseAll() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                null,
                ImmutableList.fromArray(new Object[] {new LongCell(5)})
            ),
            Command.fromSql("insert into foo values (5)")
        );
    }
    
}
