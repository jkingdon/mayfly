package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class TreeTest extends TestCase {
    private Tree asterisk;
    private Tree foo;
    private Tree bar;
    private Tree where;
    private Tree t;

    public void setUp() throws Exception {
        super.setUp();

        t = Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'");

        //( select * ( selected_table foo f ) ( selected_table bar b ) 
        // ( condition ( and ( = ( column f id ) ( column b id ) ) ( = ( column f name ) 'steve' ) ) ) )

        asterisk = new Tree(t.getFirstChild());
        foo = new Tree(asterisk.getNextSibling());
        bar = new Tree(foo.getNextSibling());
        where = new Tree(bar.getNextSibling());
    }

    public void testEquality() throws Exception {
        assertEquals(Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'"),
                     Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'"));

        assertFalse(Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'")
                    .equals(Tree.parse("select * from foo f, bar b")));
    }

    public void testChildren() throws Exception {
        Collection expectedElements = new ArrayList();
        expectedElements.add(asterisk);
        expectedElements.add(foo);
        expectedElements.add(bar);
        expectedElements.add(where);

        assertEquals(new Tree.Children(expectedElements), t.children());
    }

    public void testTypeIs() throws Exception {
        Selector typeIs = new Tree.TypeIs(SQLTokenTypes.SELECTED_TABLE);

        assertTrue(typeIs.evaluate(foo));
        assertTrue(typeIs.evaluate(bar));

        assertFalse(typeIs.evaluate(asterisk));
        assertFalse(typeIs.evaluate(where));
    }

    public void testTypeIsAnyOf() throws Exception {
        Selector typeIsAnyOf = new Tree.TypeIsAnyOf(new int[] {SQLTokenTypes.SELECTED_TABLE, SQLTokenTypes.COMMA});

        assertTrue(typeIsAnyOf.evaluate(foo));
        assertTrue(typeIsAnyOf.evaluate(bar));

        assertFalse(typeIsAnyOf.evaluate(asterisk));
        assertFalse(typeIsAnyOf.evaluate(where));
    }

    public void testSingleSubtreeOfType() throws Exception {
        assertEquals(asterisk, t.children().singleSubtreeOfType(SQLTokenTypes.ASTERISK));
    }
    
    public void testSingleSubtreeOfTypeAmbiguous() throws Exception {
        Tree tree = Tree.parse("select f.a, b.b from foo f, bar b");
        try {
            tree.children().singleSubtreeOfType(SQLTokenTypes.SELECT_ITEM);
            fail();
        } catch (RuntimeException e) {
            assertEquals("found more than one", e.getMessage());
        }
    }

    public void testConvertUsingConverters() throws Exception {
        Transformer transformer =
            new Tree.Convert(
                new TreeConverters()
                    .register(SQLTokenTypes.ASTERISK,         new TreeConverters.Converter() {
                                                                public Object convert(Tree from, TreeConverters converters) {
                                                                    return "asterisk";
                                                                }
                                                            })
            );

        assertEquals("asterisk", transformer.transform(asterisk));
    }

    public void testIgnore() throws Exception {
        Selector selector =
            new Tree.AllExceptTypes(new int[]{
                                        SQLTokenTypes.SELECTED_TABLE,
                                        SQLTokenTypes.COMMA
                                    });

        assertTrue(selector.evaluate(asterisk));
        assertTrue(selector.evaluate(where));

        assertFalse(selector.evaluate(foo));
        assertFalse(selector.evaluate(bar));
    }

}
