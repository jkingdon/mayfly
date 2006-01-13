package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
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
        Row row = new Row(
            new TupleBuilder()
                .append(new Column("x"), new StringCell("foo"))
        );
        Equal compareWithFoo = new Equal(new SingleColumn("x"), new QuotedString("'foo'"));
        Equal compareWithXxx = new Equal(new SingleColumn("x"), new QuotedString("'xxx'"));
        assertTrue(new Not(compareWithXxx).evaluate(row));
        assertFalse(new Not(compareWithFoo).evaluate(row));
    }

}
