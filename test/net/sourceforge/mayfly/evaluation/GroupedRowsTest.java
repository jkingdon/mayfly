package net.sourceforge.mayfly.evaluation;

import junit.framework.*;

import java.util.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

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
        
        Rows rows = groupedRows.ungroup(
            new Selected(Arrays.asList(new WhatElement[] {
                new SingleColumn("player"),
                new SingleColumn("year"),
                new Average(new SingleColumn("score"), "avg", false)
            }))
        );
        
        Rows expected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .append(new PositionalHeader(2), new LongCell(40))
                    .appendColumnCell("player", new StringCell("Ganguly"))
                    .appendColumnCell("year", new LongCell(2004))
                ))
                .with(new Row(new TupleBuilder()
                    .append(new PositionalHeader(2), new LongCell(0))
                    .appendColumnCell("player", new StringCell("Ganguly"))
                    .appendColumnCell("year", new LongCell(2005))
                ))
                .with(new Row(new TupleBuilder()
                    .append(new PositionalHeader(2), new LongCell(80))
                    .appendColumnCell("player", new StringCell("Tendulkar"))
                    .appendColumnCell("year", new LongCell(2004))
                ))
        );

        assertEquals(expected, rows);
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
