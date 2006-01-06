package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

public class OrTest extends TestCase {

    public void testParse() throws Exception {
        assertEquals(
                new Or(
                    new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                    new Or(
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'")),
                        new Equal(new SingleColumn("size"), new MathematicalInt(6))
                    )
                ),
                new Parser("name='steve' or species='homo sapiens' or size = 6").parseCondition()
        );
    }

    public void testEvaluate() throws Exception {
        assertTrue(new Or(new StringStartsWith("f"), new StringStartsWith("fo")).evaluate("foo"));
        assertTrue(new Or(new StringStartsWith("f"), new StringStartsWith("XX")).evaluate("foo"));
        assertTrue(new Or(new StringStartsWith("XX"), new StringStartsWith("fo")).evaluate("foo"));
        assertFalse(new Or(new StringStartsWith("XX"), new StringStartsWith("XX")).evaluate("foo"));
    }

}
