package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

public class NotEqualTest extends TestCase {
    public void testParse() throws Exception {
        assertEquals(
                new Not(new Equal(new SingleColumn("name"), new QuotedString("'steve'"))),
                new Parser("name <> 'steve'").parseCondition()
        );
    }

    public void testParse2() throws Exception {
        assertEquals(
                new Not(new Equal(new SingleColumn("name"), new QuotedString("'steve'"))),
                new Parser("name != 'steve'").parseCondition()
        );
    }

}