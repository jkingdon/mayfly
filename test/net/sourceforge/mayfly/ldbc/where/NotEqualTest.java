package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class NotEqualTest extends TestCase {

    public void testParse() throws Exception {
        Not notEqual = (Not) new Parser("name <> 'steve'").parseCondition().asBoolean();
        Equal equal = (Equal) notEqual.operand;
        MayflyAssert.assertColumn("name", equal.leftSide);
        MayflyAssert.assertString("steve", equal.rightSide);
    }

    public void testParse2() throws Exception {
        Not notEqual = (Not) new Parser("name!='steve'").parseCondition().asBoolean();
        Equal equal = (Equal) notEqual.operand;
        MayflyAssert.assertColumn("name", equal.leftSide);
        MayflyAssert.assertString("steve", equal.rightSide);
    }

}