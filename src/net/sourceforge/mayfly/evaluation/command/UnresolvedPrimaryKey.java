package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.constraint.PrimaryKey;

import java.util.List;

public class UnresolvedPrimaryKey extends UnresolvedConstraint {
    
    final ColumnNames columns;

    public UnresolvedPrimaryKey(List columns) {
        this.columns = new ColumnNames(columns);
    }
    
    PrimaryKey resolve(Columns tableColumns) {
        return new PrimaryKey(columns.resolve(tableColumns));
    }

}