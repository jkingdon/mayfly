package net.sourceforge.mayfly.evaluation;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class GroupedRowsTest extends TestCase {
    
    public void testUngroup() throws Exception {
        GroupedRows groupedRows = new GroupedRows();
        Column player = new Column("player");
        groupedRows.add(
            player,
            new StringCell("Ganguly"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .appendColumnCell("score", new LongCell(35))
            )
        );
        groupedRows.add(
            player,
            new StringCell("Ganguly"),
            new Row(new TupleBuilder()
                .append(player, new StringCell("Ganguly"))
                .appendColumnCell("score", new LongCell(45))
            )
        );
        groupedRows.add(
            player,
            new StringCell("Tendulkar"),
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

}
