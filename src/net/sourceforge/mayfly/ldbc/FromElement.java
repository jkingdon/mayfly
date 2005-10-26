package net.sourceforge.mayfly.ldbc;

import org.ldbc.antlr.collections.*;
import net.sourceforge.mayfly.util.*;

public class FromElement extends ValueObject {
    private String tableName;
    private String alias;

    public FromElement(String tableName) {
        this(tableName, null);
    }

    public FromElement(String tableName, String alias) {
        this.tableName = tableName;
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }

    public String tableName() {
        return tableName;
    }


    public static FromElement fromSeletedTableTree(Tree table) {
        AST firstIdentifier = table.getFirstChild();
        String tableName = firstIdentifier.getText();

        AST secondIdentifier = firstIdentifier.getNextSibling();

        FromElement fromElement;
        if (secondIdentifier==null) {
            fromElement = new FromElement(tableName);
        } else {
            String alias = secondIdentifier.getText();
            fromElement = new FromElement(tableName, alias);
        }
        return fromElement;
    }
}
