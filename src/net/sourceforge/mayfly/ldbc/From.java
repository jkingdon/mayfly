package net.sourceforge.mayfly.ldbc;

import org.ldbc.antlr.collections.*;

public class From extends ValueObject{
    private String tableName;
    private String alias;

    public From(String tableName) {
        this(tableName, null);
    }

    public From(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }

    public String tableName() {
        return tableName;
    }


    public static From fromSeletedTableTree(Tree table) {
        AST firstIdentifier = table.getFirstChild();
        String tableName = firstIdentifier.getText();

        AST secondIdentifier = firstIdentifier.getNextSibling();

        From from;
        if (secondIdentifier==null) {
            from = new From(tableName);
        } else {
            String alias = secondIdentifier.getText();
            from = new From(tableName, alias);
        }
        return from;
    }
}
