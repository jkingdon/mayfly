package net.sourceforge.mayfly.ldbc;

public class Dimension extends ValueObject{
    private String tableName;
    private String alias;

    public Dimension(String tableName) {
        this(tableName, null);
    }

    public Dimension(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }

    public String tableName() {
        return tableName;
    }
    
    
}
