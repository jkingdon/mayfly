package net.sourceforge.mayfly.evaluation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.expression.Average;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.Arrays;

public class GroupedRowsTest extends TestCase {
    
    public void testUngroup() throws Exception {
        GroupedRows groupedRows = new GroupedRows();
        groupedRows.add(
            keysForColumn("player"),
            new ResultRow()
                .withColumn("players", "player", new StringCell("Ganguly"))
                .withColumn("players", "score", new LongCell(35))
        );
        groupedRows.add(
            keysForColumn("player"),
            new ResultRow()
                .withColumn("players", "player", new StringCell("Ganguly"))
                .withColumn("players", "score", new LongCell(45))
        );
        groupedRows.add(
            keysForColumn("player"),
            new ResultRow()
                .withColumn("players", "player", new StringCell("Tendulkar"))
                .withColumn("players", "score", new LongCell(80))
        );
        
        ResultRows rows = groupedRows.ungroup(
            new Selected(ImmutableList.singleton(new SingleColumn("player")))
        );
        assertEquals(2, rows.size());

        ResultRow row0 = rows.row(0);
        assertEquals(1, row0.size());
        assertColumn("player", new StringCell("Ganguly"), row0, 0);

        ResultRow row1 = rows.row(1);
        assertEquals(1, row1.size());
        assertColumn("player", new StringCell("Tendulkar"), row1, 0);
    }

    public void testMultiple() throws Exception {
        GroupedRows groupedRows = new GroupedRows();
        groupedRows.add(
            keysForColumns("player", "year"),
            new ResultRow()
                .withColumn("players", "player", new StringCell("Ganguly"))
                .withColumn("players", "year", new LongCell(2004))
                .withColumn("players", "score", new LongCell(35))
        );
        groupedRows.add(
            keysForColumns("player", "year"),
            new ResultRow()
                .withColumn("players", "player", new StringCell("Ganguly"))
                .withColumn("players", "year", new LongCell(2004))
                .withColumn("players", "score", new LongCell(45))
        );
        groupedRows.add(
            keysForColumns("player", "year"),
            new ResultRow()
                .withColumn("players", "player", new StringCell("Ganguly"))
                .withColumn("players", "year", new LongCell(2005))
                .withColumn("players", "score", new LongCell(0))
        );
        groupedRows.add(
            keysForColumns("player", "year"),
            new ResultRow()
                .withColumn("players", "player", new StringCell("Tendulkar"))
                .withColumn("players", "year", new LongCell(2004))
                .withColumn("players", "score", new LongCell(80))
        );
        
        Average averageExpression = new Average(new SingleColumn("score"), "avg", false);
        ResultRows rows = groupedRows.ungroup(
            new Selected(Arrays.asList(new WhatElement[] {
                new SingleColumn("player"),
                new SingleColumn("year"),
                averageExpression
            }))
        );
        
        assertEquals(3, rows.size());
        ResultRow row0 = rows.row(0);
        ResultRow row1 = rows.row(1);
        ResultRow row2 = rows.row(2);
        
        checkRow(new LongCell(40), "Ganguly", 2004, row0, averageExpression);
        checkRow(new LongCell(0), "Ganguly", 2005, row1, averageExpression);
        checkRow(new LongCell(80), "Tendulkar", 2004, row2, averageExpression);
    }

    private void checkRow(LongCell expectedAverage, String expectedPlayer, 
        int expectedYear, ResultRow row, Average averageExpression) {
        assertEquals(3, row.size());
        assertExpression(averageExpression, expectedAverage, row, 0);
        assertColumn("player", new StringCell(expectedPlayer), row, 1);
        assertColumn("year", new LongCell(expectedYear), row, 2);
    }

    private void assertColumn(String expectedColumnName, Cell expectedCell, ResultRow row, int position) {
        SingleColumn header = (SingleColumn) row.expression(position);
        assertNull(header.tableOrAlias());
        assertEquals(expectedColumnName, header.columnName());
        assertEquals(expectedCell, row.cell(position));
    }

    private void assertExpression(Expression expected, Cell expectedCell, 
        ResultRow row, int position) {
        assertTrue(expected.sameExpression(row.expression(position)));
        assertEquals(expectedCell, row.cell(position));
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
