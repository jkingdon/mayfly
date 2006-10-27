package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.datastore.ColumnNames;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.constraint.UniqueConstraint;

import java.util.Collections;
import java.util.List;

public class UnresolvedUniqueConstraint extends UnresolvedConstraint {

    private ColumnNames constraintColumns;
    private final String constraintName;

    public UnresolvedUniqueConstraint(List columns, String constraintName) {
        this.constraintName = constraintName;
        this.constraintColumns = new ColumnNames(columns);
    }

    public UnresolvedUniqueConstraint(String column) {
        this(Collections.singletonList(column), null);
    }

    public UniqueConstraint resolve(Columns tableColumns) {
        return new UniqueConstraint(
            constraintColumns.resolve(tableColumns),
            constraintName);
    }
    
}
