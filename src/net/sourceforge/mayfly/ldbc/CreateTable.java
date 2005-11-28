package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.parser.*;

import java.util.*;

public class CreateTable extends Command {

    public static CreateTable createTableFromTree(Tree tree) {
        Iterator iter = tree.children().iterator();
        Tree table = (Tree) iter.next();
        Tree openParen = (Tree) iter.next();
        if (openParen.getType() != SQLTokenTypes.OPEN_PAREN) {
            throw new RuntimeException("Didn't expect token type " + openParen.getType());
        }
        List columnNames = new L();
        while (iter.hasNext()) {
            Tree column = (Tree) iter.next();
            Tree name = (Tree) column.children().element(0);
            columnNames.add(name.getText());
        }
        return new CreateTable(table.getText(), columnNames);
    }

    private String table;
    private List columnNames;

    public CreateTable(String table, List columnNames) {
        this.table = table;
        this.columnNames = columnNames;
    }

    public String table() {
        return table;
    }

    public List columnNames() {
        return columnNames;
    }

    public void substitute(Collection jdbcParameters) {
    }

    public DataStore update(DataStore store) {
        return store.createTable(table(), columnNames());
    }

    public int rowsAffected() {
        return 0;
    }

    public int parameterCount() {
        return 0;
    }

}
