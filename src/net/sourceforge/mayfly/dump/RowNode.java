package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.graph.Node;

public class RowNode extends Node {

    public final Row row;
    public final String tableName;
    private final Columns columns;

    public RowNode(Row row, String tableName, Columns columns) {
        this.row = row;
        this.tableName = tableName;
        this.columns = columns;
    }

    /**
     * @internal
     * Lexicographic order based on the rows (that is, compare the
     * first column, if equal then compare the second, and so on).
     */
    @Override
    public int backupOrdering(Node other) {
        Row first = row;
        Row second = ((RowNode)other).row;
        if (first == second) {
            return 0;
        }
        for (int i = 0; i < columns.columnCount(); ++i) {
            String name = columns.columnName(i);
            Cell cellFromFirst = first.cell(name);
            Cell cellFromSecond = second.cell(name);
            int comparison = cellFromFirst.compareTo(cellFromSecond);
            if (comparison != 0) {
                return comparison;
            }
        }
        throw new MayflyException("cannot dump: table " + tableName + 
            " has duplicate rows");
    }

}
