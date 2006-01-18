package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class AndTest extends TestCase {

    public void testParseWithParens() throws Exception {
        assertEquals(
                new And(
                    new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                    new And(
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'")),
                        new Equal(new SingleColumn("size"), new MathematicalInt(6))
                    )
                ),
                new Parser("name='steve' and (species='homo sapiens' and size = 6)").parseCondition()
        );
    }

    public void testParse() throws Exception {
        assertEquals(
                new And(
                    new And(
                        new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Equal(new SingleColumn("size"), new MathematicalInt(6))
                ),
                new Parser("name='steve' and species='homo sapiens' and size = 6").parseCondition()
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