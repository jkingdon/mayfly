package net.sourceforge.mayfly.ldbc;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

import java.sql.*;

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
                                new Equal(new SingleColumn("size"), new MathematicalInt(4)),
                                new Greater(new MathematicalInt(6), new SingleColumn("size"))
                            )

                        ),
                        new Or(
                            new Equal(new SingleColumn("color"), new QuotedString("'red'")),
                            new And(
                                new Not(new Equal(new SingleColumn("day"), new MathematicalInt(7))),
                                new Not(new Equal(new SingleColumn("day"), new MathematicalInt(6)))
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
                    new Equal(new SingleColumn("a"), new MathematicalInt(5))
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
                new OrderBy()
                    .add(new SingleColumn("a")),
                Limit.NONE
            ),
            Select.selectFromSql("select * from foo order by a")
        );
    }

    public void testParseSelectExpression() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new MathematicalInt(5)),
                new From()
                    .add(new FromTable("foo")),
                Where.EMPTY
            ),
            Select.selectFromSql("select 5 from foo")
        );
    }

    public void testParseJdbcParameter() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(JdbcParameter.INSTANCE),
                new From()
                    .add(new FromTable("foo")),
                new Where(
                    new Equal(new SingleColumn("a"), JdbcParameter.INSTANCE)
                )
            ),
            Select.selectFromSql("select ? from foo where a = ?")
        );
    }

    public void testParameterCount() throws Exception {
        checkParameterCount(2, "select ? from foo where a = ?");
        checkParameterCount(3, "select a from foo where (? = b or ? != c) and a > ?");
        checkParameterCount(2, "select a from foo where ? IN (1, ?, 5)");
    }

    private void checkParameterCount(int expected, String sql) throws SQLException {
        assertEquals(expected, Select.selectFromSql(sql).parameterCount());
    }

    public void testSubstituteMultipleValues() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new MathematicalInt(5)),
                new From()
                    .add(new FromTable("foo")),
                new Where(
                    new And(
                        new Or(
                            new Equal(
                                new SingleColumn("a"),
                                new MathematicalInt(6)
                            ),
                            new Not(
                                new Equal(
                                    new MathematicalInt(7),
                                    new SingleColumn("b")
                                )
                            )
                        ),
                        new Greater(
                            new MathematicalInt(8),
                            new SingleColumn("c")
                        )
                    )
                )
            ),
            substitute("select ? from foo where (a = ? or ? != b) and c < ?")
        );
    }

    public void testSubstituteIn() throws Exception {
        assertEquals(
            new Select(
                new What()
                    .add(new SingleColumn("a")),
                new From()
                    .add(new FromTable("foo")),
                new Where(
                    new In(
                        new MathematicalInt(5),
                        L.fromArray(new Object[] {
                            new MathematicalInt(6),
                            new MathematicalInt(3),
                            new MathematicalInt(7)
                        })
                    )
                )
            ),
            substitute("select a from foo where ? in (?, 3, ?)")
        );
    }

    private Command substitute(String sql) throws SQLException {
        Command command = Command.fromSql(sql);
        command.substitute(L.fromArray(new int[] { 5, 6, 7, 8 }));
        return command;
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
        return Select.selectFromSql(sql).query(store, DataStore.ANONYMOUS_SCHEMA_NAME, new What());
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
