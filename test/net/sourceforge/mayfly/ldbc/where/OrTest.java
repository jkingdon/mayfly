package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.parser.Parser;

public class OrTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new Or(
                    new Or(
                        new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Equal(new SingleColumn("size"), new IntegerLiteral(6))
                ),
                new Parser("name='steve' or species='homo sapiens' or size = 6").parseCondition().asBoolean()
        );
    }

    public void testEvaluate() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .append(new Column("x"), new StringCell("foo"))
        );
        Equal compareWithFoo = new Equal(new SingleColumn("x"), new QuotedString("'foo'"));
        Equal compareWithXxx = new Equal(new SingleColumn("x"), new QuotedString("'xxx'"));
        Not notNull = new Not(new IsNull(new SingleColumn("x")));
        assertTrue(new Or(compareWithFoo, notNull).evaluate(row));
        assertTrue(new Or(compareWithFoo, compareWithXxx).evaluate(row));
        assertTrue(new Or(compareWithXxx, notNull).evaluate(row));
        assertFalse(new Or(compareWithXxx, compareWithXxx).evaluate(row));
    }

}
