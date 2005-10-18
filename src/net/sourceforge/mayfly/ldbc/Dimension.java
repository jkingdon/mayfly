package net.sourceforge.mayfly.ldbc;

import org.ldbc.antlr.collections.*;

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


    public static Dimension fromSeletedTableTree(Tree table) {
        AST firstIdentifier = table.getFirstChild();
        String tableName = firstIdentifier.getText();

        AST secondIdentifier = firstIdentifier.getNextSibling();

        Dimension dimension;
        if (secondIdentifier==null) {
            dimension = new Dimension(tableName);
        } else {
            String alias = secondIdentifier.getText();
            dimension = new Dimension(tableName, alias);
        }
        return dimension;
    }
}
