package net.sourceforge.mayfly;

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

    public boolean tableNamesEqual(String table1, String table2) {
        if (tableNamesCaseSensitive) {
            return table1.equals(table2);
        }
        else {
            return table1.equalsIgnoreCase(table2);
        }
    }

}
