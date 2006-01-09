package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.ldbc.what.*;

public class GroupItem {

    private final SingleColumn column;

    public GroupItem(SingleColumn column) {
        this.column = column;
    }

    public SingleColumn column() {
        return column;
    }

}
