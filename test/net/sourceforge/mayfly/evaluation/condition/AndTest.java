package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.condition.And;
import net.sourceforge.mayfly.evaluation.condition.Equal;
import net.sourceforge.mayfly.evaluation.condition.IsNull;
import net.sourceforge.mayfly.evaluation.condition.Not;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class AndTest extends TestCase {

    public void testParseWithParens() throws Exception {
        And outer = (And) 
            new Parser("name='steve' and (species='homo sapiens' and size = 6)")
                .parseCondition().asBoolean();
            Equal name = (Equal) outer.leftSide;
                MayflyAssert.assertColumn("name", name.leftSide);
                MayflyAssert.assertString("steve", name.rightSide);
            And inner = (And) outer.rightSide;
                Equal species = (Equal) inner.leftSide;
                    MayflyAssert.assertColumn("species", species.leftSide);
                    MayflyAssert.assertString("homo sapiens", species.rightSide);
                Equal size = (Equal) inner.rightSide;
                    MayflyAssert.assertColumn("size", size.leftSide);
                    MayflyAssert.assertInteger(6, size.rightSide);
    }

    public void testParse() throws Exception {
        And outer = (And) 
            new Parser("name='steve' and species='homo sapiens' and size = 6")
                .parseCondition().asBoolean();
            And inner = (And) outer.leftSide;
                Equal name = (Equal) inner.leftSide;
                    MayflyAssert.assertColumn("name", name.leftSide);
                    MayflyAssert.assertString("steve", name.rightSide);
                Equal species = (Equal) inner.rightSide;
                    MayflyAssert.assertColumn("species", species.leftSide);
                    MayflyAssert.assertString("homo sapiens", species.rightSide);
            Equal size = (Equal) outer.rightSide;
                MayflyAssert.assertColumn("size", size.leftSide);
                MayflyAssert.assertInteger(6, size.rightSide);
    }

    public void testEvaluate() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCell("x", new StringCell("foo"))
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