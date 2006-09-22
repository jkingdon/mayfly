package net.sourceforge.mayfly.evaluation.select;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.util.L;

public class SelectTest extends TestCase {

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
