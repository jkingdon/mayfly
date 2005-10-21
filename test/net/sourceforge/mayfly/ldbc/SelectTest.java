package net.sourceforge.mayfly.ldbc;

import junit.framework.*;
import net.sourceforge.mayfly.ldbc.what.*;
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
                    .add(new Where.Equal(new Column("f", "name"), new Literal.QuotedString("'steve'")))
            ),
            Select.fromTree(Tree.parse("select f.*, b.name from foo f, bar b where f.name='steve'"))
        );
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
            store.table("foo").rows().join(store.table("bar").rows()),
            Select.fromTree(Tree.parse("select * from foo, bar where f.name = 'steve'")).executeOn(store)
        );

    }
}