package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.TableData;

public class UpdateTable {

    private final TableData table;
    private final int rowsAffected;

    public UpdateTable(TableData table, int rowsAffected) {
        this.table = table;
        this.rowsAffected = rowsAffected;
    }

    public TableData table() {
        return table;
    }

    public int rowsAffected() {
        return rowsAffected;
    }

}
