package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

public class RowMasksTest extends TestCase {
    //takes a row with a superset of columns and transforms into a row with a subset

    public void testFromTree() throws Exception {
        //Tree tree = Tree.parse("select f.*, b.name, b.comment from foo f, bar b");

        //System.out.println(tree.toString());


        

        //Iterable<Tree> tables = tree.children().ofType(SQLTokenTypes.SELECTED_TABLE);
        //
        //assertEquals(
        //    new Dimensions(
        //        new Dimension("foo", "f"),
        //        new Dimension("bar", "b"),
        //        new Dimension("zzz")
        //    ),
        //    Dimensions.fromTableTrees(tables)
        //);
    }
}