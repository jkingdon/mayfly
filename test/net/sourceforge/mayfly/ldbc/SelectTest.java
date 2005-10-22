package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class SelectTest extends TestCase {
    public void testParse() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new AllColumnsFromTable("f"))
                    .add(new SingleColumnExpression(new Column("b", "name"))),
                new Froms()
                    .add(new From("foo", "f"))
                    .add(new From("bar", "b")),
                new Where()
                    .add(new Equal(new Column("f", "name"), new Literal.QuotedString("'steve'")))
            ),
            Select.fromTree(Tree.parse("select f.*, b.name from foo f, bar b where f.name='steve'"))
        );
    }
    
    public void testAliasOmitted() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new SingleColumnExpression(new Column("name"))),
                new Froms()
                    .add(new From("foo")),
                new Where()
            ),
            Select.fromTree(Tree.parse("select name from foo"))
        );
    }

    // Evidently, X is reserved to ldbc (but not jsqlparser)
    public void testX() throws Exception {
        try {
            Select.fromTree(Tree.parse("select x from foo"));
            fail();
        } catch (RuntimeException e) {
            assertEquals("line 1: unexpected token: x", e.getMessage());
        }
    }

    public void testSimpleJoin() throws Exception {
        DataStore store =
            new DataStore()
                .createTable("foo", new L().append("colA").append("colB"))
                .addRow("foo", new L().append("colA").append("colB"), new L().append("1a").append("1b"))
                .addRow("foo", new L().append("colA").append("colB"), new L().append("2a").append("2b"))
                .createTable("bar", new L().append("colX").append("colY"))
                .addRow("bar", new L().append("colX").append("colY"), new L().append("1a").append("1b"))
                .addRow("bar", new L().append("colX").append("colY"), new L().append("2a").append("2b"))
                .addRow("bar", new L().append("colX").append("colY"), new L().append("3a").append("3b"));


        assertEquals(
            store.table("foo").rows().cartesianJoin(store.table("bar").rows()),
            Select.fromTree(Tree.parse("select * from foo, bar where f.name = 'steve'")).executeOn(store)
        );
    }

    public void testSimpleWhere() throws Exception {
        //DataStore store =
            new DataStore()
                .createTable("foo", new L().append("colA").append("colB"))
                .addRow("foo", new L().append("colA").append("colB"), new L().append("1a").append("1b"))
                .addRow("foo", new L().append("colA").append("colB"), new L().append("2a").append("xx"))
                .addRow("foo", new L().append("colA").append("colB"), new L().append("3a").append("xx"));


        //dont just use equal here.  it's clearer if you just make the rows the expected

        //assertEquals(
        //    store.table("foo").rows().select(new Equal(new Column("colB"), new Literal.QuotedString("2b"))),
        //    Select.fromTree(Tree.parse("select * from foo where colB = '2b'")).executeOn(store)
        //);
    }

    //TODO: probably need to resolve columns to be fully qualified, i.e. table + string

}
