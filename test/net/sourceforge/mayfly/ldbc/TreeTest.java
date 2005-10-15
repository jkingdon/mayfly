package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import java.util.*;

public class TreeTest extends TestCase {
    public void testTree() throws Exception {
        //select consists of :
            //mask.
            //dimensions.
            //constraint.

        Tree t = Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'");

        System.out.println(t.toString());
    }

    public void testEquality() throws Exception {
        assertEquals(Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'"),
                     Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'"));

        assertFalse(Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'")
                    .equals(Tree.parse("select * from foo f, bar b")));
    }

    public void testChildren() throws Exception {
        Tree t = Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'");


        Tree asterisk = new Tree(t.getFirstChild());
        Tree foo = new Tree(asterisk.getNextSibling());
        Tree comma = new Tree(foo.getNextSibling());
        Tree bar = new Tree(comma.getNextSibling());
        Tree where = new Tree(bar.getNextSibling());

        Collection<Tree> expectedElements = new ArrayList<Tree>();
        expectedElements.add(asterisk);
        expectedElements.add(foo);
        expectedElements.add(comma);
        expectedElements.add(bar);
        expectedElements.add(where);

        assertEquals(new Tree.Children(expectedElements), t.children());
    }



}