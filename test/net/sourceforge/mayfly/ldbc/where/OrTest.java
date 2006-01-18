package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class OrTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new Or(
                    new Or(
                        new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Equal(new SingleColumn("size"), new MathematicalInt(6))
                ),
                new Parser("name='steve' or species='homo sapiens' or size = 6").parseCondition()
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
