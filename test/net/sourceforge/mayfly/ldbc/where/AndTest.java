package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.*;

public class AndTest extends TestCase {
    public void testParseWithParens() throws Exception {
        assertEquals(
                new And(
                    new And(
                        new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'"))
                    ),
                    new Equal(new SingleColumn("size"), new MathematicalInt(6))
                ),
                new Parser("(name='steve' and species='homo sapiens') and size = 6").parseCondition()
        );
    }

    public void testParse() throws Exception {
        assertEquals(
                new And(
                    new Equal(new SingleColumn("name"), new QuotedString("'steve'")),
                    new And(
                        new Equal(new SingleColumn("species"), new QuotedString("'homo sapiens'")),
                        new Equal(new SingleColumn("size"), new MathematicalInt(6))
                    )
                ),
                new Parser("name='steve' and species='homo sapiens' and size = 6").parseCondition()
        );
    }

    public void testEval() throws Exception {
        assertTrue(new And(new StringStartsWith("f"), new StringStartsWith("fo")).evaluate("foo"));
        assertFalse(new And(new StringStartsWith("f"), new StringStartsWith("XX")).evaluate("foo"));
        assertFalse(new And(new StringStartsWith("XX"), new StringStartsWith("fo")).evaluate("foo"));
        assertFalse(new And(new StringStartsWith("XX"), new StringStartsWith("XX")).evaluate("foo"));
    }

}