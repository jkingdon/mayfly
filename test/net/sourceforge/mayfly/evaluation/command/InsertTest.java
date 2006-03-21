package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCellContent;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.Insert;
import net.sourceforge.mayfly.evaluation.command.InsertTable;

import java.util.Arrays;

public class InsertTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Arrays.asList(new String[] {"a", "b"}),
                Arrays.asList(new Object[] {new Long(5), "Value"})
            ),
            Command.fromSql("insert into foo (a, b) values (5, 'Value')")
        );
    }
    
    public void testParseNull() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Arrays.asList(new String[] {"a"}),
                Arrays.asList(new Object[] {NullCellContent.INSTANCE})
            ),
            Command.fromSql("insert into foo (a) values (null)")
        );
    }
    
    public void testParseAll() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                null,
                Arrays.asList(new Object[] {new Long(5)})
            ),
            Command.fromSql("insert into foo values (5)")
        );
    }
    
}
