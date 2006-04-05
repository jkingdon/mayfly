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

public class AndTest extends TestCase {

    public void testParseWithParens() throws Exception {
        assertEquals(
                new And(
                    new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                    new And(
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'")),
                        new Equal(new SingleColumn("size"), new IntegerLiteral(6))
                    )
                ),
                new Parser("name='steve' and (species='homo sapiens' and size = 6)").parseCondition().asBoolean()
        );
    }

    public void testParse() throws Exception {
        assertEquals(
                new And(
                    new And(
                        new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Equal(new SingleColumn("size"), new IntegerLiteral(6))
                ),
                new Parser("name='steve' and species='homo sapiens' and size = 6").parseCondition().asBoolean()
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
        assertTrue(new And(compareWithFoo, notNull).evaluate(row));
        assertFalse(new And(compareWithFoo, compareWithXxx).evaluate(row));
        assertFalse(new And(compareWithXxx, notNull).evaluate(row));
        assertFalse(new And(compareWithXxx, compareWithXxx).evaluate(row));
    }

}