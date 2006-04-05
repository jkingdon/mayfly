package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.parser.Parser;

public class NotTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new Not(new Equal(new SingleColumn("name"), new QuotedString("'jim'"))),
            new Parser("not name = 'jim'").parseCondition().asBoolean()
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
