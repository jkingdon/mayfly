package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Rows;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.PositionalHeader;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Arrays;

public class GroupedRowsTest extends TestCase {
    
    public void testUngroup() throws Exception {
        GroupedRows groupedRows = new GroupedRows();
        Column player = new Column("player");
        groupedRows.add(
            keysForColumn("player"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .appendColumnCell("score", new LongCell(35))
            )
        );
        groupedRows.add(
            keysForColumn("player"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .appendColumnCell("score", new LongCell(45))
            )
        );
        groupedRows.add(
            keysForColumn("player"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Tendulkar"))
                .appendColumnCell("score", new LongCell(80))
            )
        );
        
        Rows rows = groupedRows.ungroup(new Selected(ImmutableList.singleton(new SingleColumn("player"))));
        
        Rows expected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("player", new StringCell("Ganguly"))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("player", new StringCell("Tendulkar"))
                ))
        );

        assertEquals(expected, rows);
    }

    public void testMultiple() throws Exception {
        GroupedRows groupedRows = new GroupedRows();
        Column player = new Column("player");
        Column year = new Column("year");
        groupedRows.add(
            keysForColumns("player", "year"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .append(year, new LongCell(2004))
                .appendColumnCell("score", new LongCell(35))
            )
        );
        groupedRows.add(
            keysForColumns("player", "year"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .append(year, new LongCell(2004))
                .appendColumnCell("score", new LongCell(45))
            )
        );
        groupedRows.add(
            keysForColumns("player", "year"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .append(year, new LongCell(2005))
                .appendColumnCell("score", new LongCell(0))
            )
        );
        groupedRows.add(
            keysForColumns("player", "year"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Tendulkar"))
                .append(year, new LongCell(2004))
                .appendColumnCell("score", new LongCell(80))
            )
        );
        
        Average averageExpression = new Average(new SingleColumn("score"), "avg", false);
        Rows rows = groupedRows.ungroup(
            new Selected(Arrays.asList(new WhatElement[] {
                new SingleColumn("player"),
                new SingleColumn("year"),
                averageExpression
            }))
        );
        
        assertEquals(3, rows.size());
        Row row0 = (Row) rows.element(0);
        Row row1 = (Row) rows.element(1);
        Row row2 = (Row) rows.element(2);
        
        checkRow(new LongCell(40), "Ganguly", 2004, row0, averageExpression);
        checkRow(new LongCell(0), "Ganguly", 2005, row1, averageExpression);
        checkRow(new LongCell(80), "Tendulkar", 2004, row2, averageExpression);
    }

    private void checkRow(LongCell expectedAverage, String expectedPlayer, int expectedYear, Row row, Average averageExpression) {
        assertEquals(3, row.size());
        assertExpression(averageExpression, expectedAverage, row, 0);
        assertColumn("player", new StringCell(expectedPlayer), row, 1);
        assertColumn("year", new LongCell(expectedYear), row, 2);
    }

    private void assertColumn(String expectedColumnName, Cell expectedCell, Row row, int position) {
        TupleElement expressionAndValue = (TupleElement) row.element(position);
        Column header = (Column) expressionAndValue.header();
        assertNull(header.tableOrAlias());
        assertEquals(expectedColumnName, header.columnName());
        assertEquals(expectedCell, expressionAndValue.cell());
    }

    private void assertExpression(Expression expected, Cell expectedCell, Row row0, int position) {
        TupleElement expressionAndValue = (TupleElement) row0.element(position);
        PositionalHeader header = (PositionalHeader) expressionAndValue.header();
        assertTrue(header.expression.sameExpression(expected));
        assertEquals(expectedCell, expressionAndValue.cell());
    }

    private GroupByKeys keysForColumn(String columnName) {
        GroupByKeys keys = new GroupByKeys();
        keys.add(new GroupItem(new SingleColumn(columnName)));
        return keys;
    }

    private GroupByKeys keysForColumns(String columnName1, String columnName2) {
        GroupByKeys keys = new GroupByKeys();
        keys.add(new GroupItem(new SingleColumn(columnName1)));
        keys.add(new GroupItem(new SingleColumn(columnName2)));
        return keys;
    }

}
