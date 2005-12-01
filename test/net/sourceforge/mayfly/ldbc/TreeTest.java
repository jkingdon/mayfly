package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class TreeTest extends TestCase {
    private Tree asterisk;
    private Tree foo;
    private Tree comma;
    private Tree bar;
    private Tree where;
    private Tree t;

    public void setUp() throws Exception {
        super.setUp();

        t = Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'");


        asterisk = new Tree(t.getFirstChild());
        foo = new Tree(asterisk.getNextSibling());
        comma = new Tree(foo.getNextSibling());
        bar = new Tree(comma.getNextSibling());
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
        expectedElements.add(comma);
        expectedElements.add(bar);
        expectedElements.add(where);

        assertEquals(new Tree.Children(expectedElements), t.children());
    }

    public void testTypeIs() throws Exception {
        Selector typeIs = new Tree.TypeIs(SQLTokenTypes.SELECTED_TABLE);

        assertTrue(typeIs.evaluate(foo));
        assertTrue(typeIs.evaluate(bar));

        assertFalse(typeIs.evaluate(asterisk));
        assertFalse(typeIs.evaluate(comma));
        assertFalse(typeIs.evaluate(where));
    }

    public void testTypeIsAnyOf() throws Exception {
        Selector typeIsAnyOf = new Tree.TypeIsAnyOf(new int[] {SQLTokenTypes.SELECTED_TABLE, SQLTokenTypes.COMMA});

        assertTrue(typeIsAnyOf.evaluate(foo));
        assertTrue(typeIsAnyOf.evaluate(bar));
        assertTrue(typeIsAnyOf.evaluate(comma));

        assertFalse(typeIsAnyOf.evaluate(asterisk));
        assertFalse(typeIsAnyOf.evaluate(where));
    }

    public void testSingleSubtreeOfType() throws Exception {
        assertEquals(comma, t.children().singleSubtreeOfType(SQLTokenTypes.COMMA));
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
                    .register(SQLTokenTypes.COMMA,          new TreeConverters.Converter() {
                                                        public Object convert(Tree from, TreeConverters converters) {
                                                            return "comma";
                                                        }
                                                    }));

        assertEquals("asterisk", transformer.transform(asterisk));
        assertEquals("comma", transformer.transform(comma));
    }

    public void testIgnore() throws Exception {
        Selector selector =
            new Tree.AllExceptTypes(new int[]{
                                        SQLTokenTypes.SELECTED_TABLE,
                                        SQLTokenTypes.COMMA
                                    });

        assertTrue(selector.evaluate(asterisk));
        assertTrue(selector.evaluate(where));

        assertFalse(selector.evaluate(comma));
        assertFalse(selector.evaluate(foo));
        assertFalse(selector.evaluate(bar));
    }



}