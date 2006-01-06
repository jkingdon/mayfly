package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.*;
import net.sourceforge.mayfly.ldbc.*;

public class SingleColumn extends Expression {
    private String tableOrAlias;
    private String columnName;

    public SingleColumn(String columnName) {
        this(null, columnName);
    }

    public SingleColumn(String tableOrAlias, String columnName) {
        this.tableOrAlias = tableOrAlias;
        this.columnName = columnName;
    }

    public Cell evaluate(Row row) {
        return row.cell(tableOrAlias, columnName);
    }
    
    public Cell aggregate(Rows rows) {
        throw new MayflyInternalException("shouldn't combine aggregate and column expressions");
    }

    public String firstColumn() {
        return displayName();
    }

    public String displayName() {
        return Column.displayName(tableOrAlias, columnName);
    }

}
