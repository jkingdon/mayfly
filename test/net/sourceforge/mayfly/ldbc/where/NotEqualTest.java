package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.where.literal.QuotedString;
import net.sourceforge.mayfly.parser.Parser;

public class NotEqualTest extends TestCase {
    public void testParse() throws Exception {
        assertEquals(
                new Not(new Equal(new SingleColumn("name"), new QuotedString("'steve'"))),
                new Parser("name <> 'steve'").parseCondition().asBoolean()
        );
    }

    public void testParse2() throws Exception {
        assertEquals(
                new Not(new Equal(new SingleColumn("name"), new QuotedString("'steve'"))),
                new Parser("name != 'steve'").parseCondition().asBoolean()
        );
    }

}