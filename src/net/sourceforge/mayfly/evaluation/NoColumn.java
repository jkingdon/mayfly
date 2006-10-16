package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.parser.Location;

public class NoColumn extends MayflyException {

    public NoColumn(String name, Location location) {
        super("no column " + name, location);
    }
    public NoColumn(String tableOrAlias, String columnName, Location location) {
        this(
            Column.displayName(tableOrAlias, columnName), 
            location);
    }

    public NoColumn(String columnName) {
        this(columnName, Location.UNKNOWN);
    }

}
