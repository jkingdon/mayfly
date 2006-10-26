package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.constraint.UniqueConstraint;

import java.util.List;

public class UnresolvedUniqueConstraint {

    private ColumnNames constraintColumns;

    public UnresolvedUniqueConstraint(List columns) {
        this.constraintColumns = new ColumnNames(columns);
    }

    public UniqueConstraint resolve(Columns tableColumns) {
        return new UniqueConstraint(constraintColumns.resolve(tableColumns));
    }
    
}