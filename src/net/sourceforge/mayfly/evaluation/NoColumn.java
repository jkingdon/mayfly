package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.parser.Location;

public class NoColumn extends MayflyException {

    private final String displayName;

    public NoColumn(String name, Location location) {
        super("no column " + name, location);
        this.displayName = name;
    }

    public NoColumn(String tableOrAlias, String columnName, Location location) {
        this(
            Column.displayName(tableOrAlias, columnName), 
            location);
    }

    public NoColumn(String columnName) {
        this(columnName, Location.UNKNOWN);
    }
    
    private NoColumn(String message, Location location, boolean isDummy) {
        super(message, location);
        displayName = null;
    }

    public static NoColumn dummyExceptionForSubselect(Location location) {
        return new NoColumn(
            "The query optimizer shouldn't try to move subselects", 
            location, true);
    }
    
    public String displayName() {
        return displayName;
    }

}
