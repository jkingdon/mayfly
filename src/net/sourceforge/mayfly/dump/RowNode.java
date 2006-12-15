package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.graph.Node;

public class RowNode extends Node {

    public final Row row;
    public final String tableName;

    public RowNode(Row row, String tableName) {
        this.row = row;
        this.tableName = tableName;
    }

    /**
     * @internal
     * Lexicographic order based on the rows (that is, compare the
     * first column, if equal then compare the second, and so on).
     */
    public int backupOrdering(Node other) {
        Row first = row;
        Row second = ((RowNode)other).row;
        if (first == second) {
            return 0;
        }
        for (int i = 0; i < first.columnCount(); ++i) {
            Cell cellFromFirst = first.cell(i);
            Cell cellFromSecond = second.cell(i);
            int comparison = cellFromFirst.compareTo(cellFromSecond);
            if (comparison != 0) {
                return comparison;
            }
        }
        throw new MayflyException("cannot dump: table " + tableName + 
            " has duplicate rows");
    }

}
