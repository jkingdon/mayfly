package net.sourceforge.mayfly.ldbc;

import java.sql.*;
import java.util.*;

import net.sourceforge.mayfly.datastore.*;

public class DropTable extends Command {

    public static DropTable dropTableFromTree(Tree tree) {
        Tree.Children children = tree.children();
        Tree tableIdentifier = (Tree) children.element(0);
        return new DropTable(tableIdentifier.getText());
    }

    private final String table;

    public DropTable(String table) {
        this.table = table;
    }
    
    public String table() {
        return table;
    }

    public void substitute(Collection jdbcParameters) {
    }

    public DataStore update(DataStore store) throws SQLException {
        return store.dropTable(table());
    }

    public int rowsAffected() {
        return 0;
    }

    public int parameterCount() {
        return 0;
    }

}
