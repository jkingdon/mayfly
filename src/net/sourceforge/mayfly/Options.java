package net.sourceforge.mayfly;

import net.sourceforge.mayfly.datastore.Schema;

public class Options {

    private final boolean tableNamesCaseSensitive;
    
    public Options() {
        this(false);
    }

    public Options(boolean tableNamesCaseSensitive) {
        this.tableNamesCaseSensitive = tableNamesCaseSensitive;
    }

    public boolean tableNamesCaseSensitive() {
        return tableNamesCaseSensitive;
    }

    public Options tableNamesCaseSensitive(boolean caseSensitive) {
        return new Options(caseSensitive);
    }

    /**
     * @internal
     * This method is hard to use correctly, because a return value
     * of false can mean either that there is no table with that name,
     * or that there is a table but we are referring to it incorrectly
     * (that is, the wrong case).  Methods like 
     * {@link Schema#lookUpTable(String, net.sourceforge.mayfly.parser.Location, Options)}
     * do not suffer from this problem.
     */
    public boolean tableNamesEqual(String table1, String table2) {
        if (tableNamesCaseSensitive) {
            return table1.equals(table2);
        }
        else {
            return table1.equalsIgnoreCase(table2);
        }
    }

}
