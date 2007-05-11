package net.sourceforge.mayfly.datastore;

public class Index {

    private final String name;
    public final ColumnNames columns;

    public Index(String name, ColumnNames columns) {
        this.name = name;
        this.columns = columns;
    }

    public boolean hasName() {
        return name != null;
    }
    
    public String name() {
        return name;
    }

}
