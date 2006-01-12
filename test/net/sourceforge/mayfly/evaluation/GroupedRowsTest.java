package net.sourceforge.mayfly.evaluation;

import junit.framework.*;

import java.util.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class GroupedRowsTest extends TestCase {
    
    public void testUngroup() throws Exception {
        GroupedRows groupedRows = new GroupedRows();
        Column player = new Column("player");
        groupedRows.add(
            player,
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .appendColumnCell("score", new LongCell(35))
            )
        );
        groupedRows.add(
            player,
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .appendColumnCell("score", new LongCell(45))
            )
        );
        groupedRows.add(
            player,
            new Row(new TupleBuilder()
                .append(player, new StringCell("Tendulkar"))
                .appendColumnCell("score", new LongCell(80))
            )
        );
        
        Rows rows = groupedRows.ungroup(new What(ImmutableList.singleton(new SingleColumn("player"))));
        
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
            Arrays.asList(new Column[] { player, year }),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .append(year, new LongCell(2004))
                .appendColumnCell("score", new LongCell(35))
            )
        );
        groupedRows.add(
            Arrays.asList(new Column[] { player, year }),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .append(year, new LongCell(2004))
                .appendColumnCell("score", new LongCell(45))
            )
        );
        groupedRows.add(
            Arrays.asList(new Column[] { player, year }),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .append(year, new LongCell(2005))
                .appendColumnCell("score", new LongCell(0))
            )
        );
        groupedRows.add(
            Arrays.asList(new Column[] { player, year }),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Tendulkar"))
                .append(year, new LongCell(2004))
                .appendColumnCell("score", new LongCell(80))
            )
        );
        
        Rows rows = groupedRows.ungroup(
            new What(Arrays.asList(new WhatElement[] {
                new SingleColumn("player"),
                new SingleColumn("year"),
                new Average(new SingleColumn("score"), "avg", false)
            }))
        );
        
        Rows expected = new Rows(
            new ImmutableList()
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("player", new StringCell("Ganguly"))
                    .appendColumnCell("year", new LongCell(2004))
                    .append(new PositionalHeader(2), new LongCell(40))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("player", new StringCell("Ganguly"))
                    .appendColumnCell("year", new LongCell(2005))
                    .append(new PositionalHeader(2), new LongCell(0))
                ))
                .with(new Row(new TupleBuilder()
                    .appendColumnCell("player", new StringCell("Tendulkar"))
                    .appendColumnCell("year", new LongCell(2004))
                    .append(new PositionalHeader(2), new LongCell(80))
                ))
        );

        assertEquals(expected, rows);
    }

}
