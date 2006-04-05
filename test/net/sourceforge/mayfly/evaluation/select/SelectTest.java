package net.sourceforge.mayfly.evaluation.select;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.NoGroupBy;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.from.From;
import net.sourceforge.mayfly.evaluation.from.FromTable;
import net.sourceforge.mayfly.evaluation.from.InnerJoin;
import net.sourceforge.mayfly.evaluation.from.LeftJoin;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.what.All;
import net.sourceforge.mayfly.ldbc.what.AllColumnsFromTable;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.what.What;
import net.sourceforge.mayfly.ldbc.where.And;
import net.sourceforge.mayfly.ldbc.where.Equal;
import net.sourceforge.mayfly.ldbc.where.Greater;
import net.sourceforge.mayfly.ldbc.where.Not;
import net.sourceforge.mayfly.ldbc.where.Or;
import net.sourceforge.mayfly.ldbc.where.Where;
import net.sourceforge.mayfly.ldbc.where.literal.QuotedString;
import net.sourceforge.mayfly.util.L;

public class SelectTest extends TestCase {

    public void testGrandParseIntegration() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new AllColumnsFromTable("f"))
                    .add(new SingleColumn("b", "name")),
                new From()
                    .add(new FromTable("foo", "f"))
                    .add(new FromTable("bar", "b")),
                new Where(
                    new And(
                        new And(
                            new Equal(new SingleColumn("f", "name"), new QuotedString("'steve'")),
                            new Or(
                                new Equal(new SingleColumn("size"), new IntegerLiteral(4)),
                                new Greater(new IntegerLiteral(6), new SingleColumn("size"))
                            )

                        ),
                        new Or(
                            new Equal(new SingleColumn("color"), new QuotedString("'red'")),
                            new And(
                                new Not(new Equal(new SingleColumn("day"), new IntegerLiteral(7))),
                                new Not(new Equal(new SingleColumn("day"), new IntegerLiteral(6)))
                            )

                        )
                    )

                )
            ),
            Select.selectFromSql("select f.*, b.name from foo f, bar b " +
                                       "where (f.name='steve' and " +
                                                " (size = 4 or 6 >size ) ) " +
                                             " and " +
                                                 "(color='red' or " +
                                                            " (day <>7 and day != 6) )")
        );
    }

    public void testParseIntegerLiteral() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new All()),
                new From()
                    .add(new FromTable("foo")),
                new Where(
                    new Equal(new SingleColumn("a"), new IntegerLiteral(5))
                )
            ),
            Select.selectFromSql("select * from foo where a = 5")
        );
    }

    public void testAliasOmitted() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new SingleColumn("name")),
                new From()
                    .add(new FromTable("foo")),
                Where.EMPTY
            ),
            Select.selectFromSql("select name from foo")
        );
    }

    public void testOrderBy() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new All()),
                new From()
                    .add(new FromTable("foo")),
                Where.EMPTY,
                new NoGroupBy(),
                new OrderBy()
                    .add(new SingleColumn("a")), Limit.NONE
            ),
            Select.selectFromSql("select * from foo order by a")
        );
    }

    public void testParseSelectExpression() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new IntegerLiteral(5)),
                new From()
                    .add(new FromTable("foo")),
                Where.EMPTY
            ),
            Select.selectFromSql("select 5 from foo")
        );
    }

    // Some versions of ldbc reserved X, but seemingly not the grammar
    // we have now.
    public void testX() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new SingleColumn("x")),
                new From()
                    .add(new FromTable("foo")),
                Where.EMPTY
            ),
            Select.selectFromSql("select x from foo")
        );
    }
    
    public void testParseExplicitJoin() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new All()),
                new From()
                    .add(new InnerJoin(
                        new FromTable("places"),
                        new FromTable("types"),
                        new Where(
                            new Equal(new SingleColumn("type"), new SingleColumn("id"))
                        )
                    )),
                Where.EMPTY
            ),
            Select.selectFromSql(
                "select * from places inner join types on type = id"
            )
        );
    }
    
    public void testParseLeftJoin() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new All()),
                new From()
                    .add(new LeftJoin(
                        new FromTable("places"),
                        new FromTable("types"),
                        new Where(
                            new Equal(new SingleColumn("type"), new SingleColumn("id"))
                        )
                    )),
                Where.EMPTY
            ),
            Select.selectFromSql(
                "select * from places left outer join types on type = id"
            )
        );
    }
    
    public void testNestedJoins() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new All()),
                new From()
                    .add(new InnerJoin(
                        new InnerJoin(
                            new FromTable("foo"),
                            new FromTable("bar"),
                            new Where(
                                new Equal(new SingleColumn("f"), new SingleColumn("b1"))
                            )
                        ),
                        new FromTable("quux"),
                        new Where(
                            new Equal(new SingleColumn("b2"), new SingleColumn("q"))
                        )
                    )),
                Where.EMPTY
            ),
            Select.selectFromSql(
                "select * from foo inner join bar on f = b1 inner join quux on b2 = q"
            )
        );
    }

    public void testExecuteSimpleJoin() throws Exception {
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", new L().append("colA").append("colB"))
                    .addRow("foo", new L().append("colA").append("colB"), new L().append("1a").append("1b"))
                    .addRow("foo", new L().append("colA").append("colB"), new L().append("2a").append("2b"))
                    .createTable("bar", new L().append("colX").append("colY"))
                    .addRow("bar", new L().append("colX").append("colY"), new L().append("1a").append("1b"))
                    .addRow("bar", new L().append("colX").append("colY"), new L().append("2a").append("2b"))
                    .addRow("bar", new L().append("colX").append("colY"), new L().append("3a").append("3b"))
            );

        assertEquals(
            store.table("foo").rows().cartesianJoin(store.table("bar").rows()),
            query(store, "select * from foo, bar")
        );
    }

    private Rows query(DataStore store, String sql) {
        return Select.selectFromSql(sql).query(store, DataStore.ANONYMOUS_SCHEMA_NAME, new Selected());
    }

    public void testSmallerJoin() throws Exception {
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", new L().append("colA"))
                    .addRow("foo", new L().append("colA"), new L().append("1a"))
                    .createTable("bar", new L().append("colX"))
                    .addRow("bar", new L().append("colX"), new L().append("barXValue"))
                );


        assertEquals(
            new Rows(
                new L()
                    .append(new Row(
                                new TupleBuilder()
                                    .appendColumnCellContents("foo", "colA", "1a")
                                    .appendColumnCellContents("bar", "colX", "barXValue"))
                ).asImmutable()
            ),
            query(store, "select * from foo, bar")
        );
    }

    public void testSimpleWhere() throws Exception {
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", new L().append("colA").append("colB"))
                    .addRow("foo", new L().append("colA").append("colB"), new L().append("1a").append("1b"))
                    .addRow("foo", new L().append("colA").append("colB"), new L().append("2a").append("xx"))
                    .addRow("foo", new L().append("colA").append("colB"), new L().append("3a").append("xx"))
            );

        assertEquals(
            store.table("foo").rows().elements(new int[]{1, 2}),
            query(store, "select * from foo where colB = 'xx'")
        );
    }


    //TODO: probably need to resolve columns to be fully qualified, i.e. table + string
    
}
