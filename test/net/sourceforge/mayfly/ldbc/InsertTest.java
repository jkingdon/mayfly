package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class InsertTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Arrays.asList(new String[] {"a", "b"}),
                Arrays.asList(new Object[] {new Long(5), "Value"})
            ),
            Insert.insertFromTree(Tree.parse("insert into foo (a, b) values (5, 'Value')"))
        );
    }
    
    public void testParseJdbcParameter() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Collections.singletonList("a"),
                Collections.singletonList(JdbcParameter.INSTANCE)
            ),
            Insert.insertFromTree(Tree.parse("insert into foo (a) values (?)"))
        );
    }
    
    public void testSubstitute() throws Exception {
        Insert insert = new Insert(
            new InsertTable("foo"),
            Collections.singletonList("a"),
            new L().append(JdbcParameter.INSTANCE)
        );
        insert.substitute(Collections.singletonList(new Long(77)));
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Collections.singletonList("a"),
                Collections.singletonList(new Long(77))
            ),
            insert
        );
    }
    
    public void testParseNull() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Arrays.asList(new String[] {"a"}),
                Arrays.asList(new Object[] {NullCellContent.INSTANCE})
            ),
            Insert.insertFromTree(Tree.parse("insert into foo (a) values (null)"))
        );
    }
    
}
