package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class NotTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new Not(new Equal(new SingleColumn("name"), new QuotedString("'jim'"))),
            new Parser("not name = 'jim'").parseCondition()
        );
    }
    
    public void testEvaluate() throws Exception {
        assertTrue(new Not(new StringStartsWith("f")).evaluate("bar"));
        assertFalse(new Not(new StringStartsWith("f")).evaluate("foo"));
    }

}
