package net.sourceforge.mayfly.ldbc;

import junit.framework.TestCase;

import net.sourceforge.mayfly.parser.*;

public class TreeConvertersTest extends TestCase {
    private Tree asterisk;
    private Tree foo;
    private Tree bar;
    private Tree where;
    private Tree t;

    public void setUp() throws Exception {
        super.setUp();

        t = Tree.parse("select * from foo f, bar b where f.id=b.id and f.name='steve'");

        asterisk = new Tree(t.getFirstChild());
        foo = new Tree(asterisk.getNextSibling());
        bar = new Tree(foo.getNextSibling());
        where = new Tree(bar.getNextSibling());
    }

    public void testTransform() throws Exception {
        TreeConverters treeConverters =
            new TreeConverters()
                .register(SQLTokenTypes.SELECTED_TABLE,
                          new TreeConverters.Converter() {
                              public Object convert(Tree from, TreeConverters converters) {
                                  return from.getFirstChild().getText();
                              }
                          });

        assertTrue(treeConverters.canTransform(foo));
        assertTrue(treeConverters.canTransform(bar));

        assertFalse(treeConverters.canTransform(asterisk));
        assertFalse(treeConverters.canTransform(where));

        assertEquals("foo", treeConverters.transform(foo));
        assertEquals("bar", treeConverters.transform(bar));
    }

}