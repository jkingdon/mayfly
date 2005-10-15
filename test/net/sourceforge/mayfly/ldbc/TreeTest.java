package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import org.ldbc.parser.*;

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

    public void testTree() throws Exception {
        //select consists of :
            //mask.
            //dimensions.
            //constraint.

        System.out.println(t.toString());
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



}