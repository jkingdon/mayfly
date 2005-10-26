package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.*;
import org.ldbc.parser.*;

public class MathematicalIntTest extends TestCase {
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where size = 5");

        Tree decimalValueTree =
            selectTree.children()
                .singleSubtreeOfType(SQLTokenTypes.CONDITION).children()
                    .singleSubtreeOfType(SQLTokenTypes.EQUAL).children()
                        .singleSubtreeOfType(SQLTokenTypes.DECIMAL_VALUE);

        assertEquals(new MathematicalInt(5), MathematicalInt.fromDecimalValueTree(decimalValueTree));
    }

    public void testValue() throws Exception {
        assertEquals(new Long(5), new MathematicalInt(5).valueForCellContentComparison());
    }
}