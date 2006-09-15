package net.sourceforge.mayfly.evaluation.select;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.NoGroupBy;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.QuotedString;
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
        L fooColumns = new L().append("colA").append("colB");
        L barColumns = new L().append("colX").append("colY");
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", fooColumns)
                    .addRow("foo", fooColumns, makeValues("1a", "1b"))
                    .addRow("foo", fooColumns, makeValues("2a", "2b"))

                    .createTable("bar", barColumns)
                    .addRow("bar", barColumns, makeValues("1a", "1b"))
                    .addRow("bar", barColumns, makeValues("2a", "2b"))
                    .addRow("bar", barColumns, makeValues("3a", "3b"))
            );

        ResultRows rows = query(store, "select * from foo, bar");
        assertEquals(6, rows.size());
        assertRow("foo", "colA", "1a", "foo", "colB", "1b", "bar", "colX", "1a", "bar", "colY", "1b", rows.row(0));
        assertRow("foo", "colA", "1a", "foo", "colB", "1b", "bar", "colX", "2a", "bar", "colY", "2b", rows.row(1));
        assertRow("foo", "colA", "1a", "foo", "colB", "1b", "bar", "colX", "3a", "bar", "colY", "3b", rows.row(2));
        assertRow("foo", "colA", "2a", "foo", "colB", "2b", "bar", "colX", "1a", "bar", "colY", "1b", rows.row(3));
        assertRow("foo", "colA", "2a", "foo", "colB", "2b", "bar", "colX", "2a", "bar", "colY", "2b", rows.row(4));
        assertRow("foo", "colA", "2a", "foo", "colB", "2b", "bar", "colX", "3a", "bar", "colY", "3b", rows.row(5));
    }

    private void assertRow(String alias1, String column1, String value1, 
        String alias2, String column2, String value2, 
        String alias3, String column3, String value3, 
        String alias4, String column4, String value4, 
        ResultRow row) {
        assertEquals(4, row.size());
        assertRowElement(alias1, column1, value1, row.element(0));
        assertRowElement(alias2, column2, value2, row.element(1));
        assertRowElement(alias3, column3, value3, row.element(2));
        assertRowElement(alias4, column4, value4, row.element(3));
    }

    private void assertRow(String alias1, String column1, String value1, 
        String alias2, String column2, String value2, 
        ResultRow row) {
        assertEquals(2, row.size());
        assertRowElement(alias1, column1, value1, row.element(0));
        assertRowElement(alias2, column2, value2, row.element(1));
    }

    private void assertRowElement(String alias, String column, String value, ResultRow.Element element) {
        assertEquals(alias, element.column().tableOrAlias());
        assertEquals(column, element.column().columnName());
        assertEquals(value, element.value.asString());
    }

    private ResultRows query(DataStore store, String sql) {
        return Select.selectFromSql(sql).query(store, DataStore.ANONYMOUS_SCHEMA_NAME, new Selected());
    }

    public void testSmallerJoin() throws Exception {
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", new L().append("colA"))
                    .addRow("foo", new L().append("colA"), new L().append(new StringCell("1a")))

                    .createTable("bar", new L().append("colX"))
                    .addRow("bar", new L().append("colX"), new L().append(new StringCell("barXValue")))
                );


        ResultRows rows = query(store, "select * from foo, bar");
        assertEquals(1, rows.size());
        assertRow("foo", "colA", "1a", "bar", "colX", "barXValue", rows.row(0));
    }

    public void testSimpleWhere() throws Exception {
        L columnNames = new L().append("colA").append("colB");
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", columnNames)
                    .addRow("foo", columnNames, makeValues("1a", "1b"))
                    .addRow("foo", columnNames, makeValues("2a", "xx"))
                    .addRow("foo", columnNames, makeValues("3a", "xx"))
            );

        ResultRows rows = query(store, "select * from foo where colB = 'xx'");
        assertEquals(2, rows.size());
        assertRow("foo", "colA", "2a", "foo", "colB", "xx", rows.row(0));
        assertRow("foo", "colA", "3a", "foo", "colB", "xx", rows.row(1));
    }

    private L makeValues(String firstStringValue, String secondStringValue) {
        return new L().append(new StringCell(firstStringValue)).append(new StringCell(secondStringValue));
    }

}
