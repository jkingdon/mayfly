package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;

import java.util.*;

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

    public DataStore update(DataStore store, String schema) {
        return store.dropTable(schema, table());
    }

    public int rowsAffected() {
        return 0;
    }

    public int parameterCount() {
        return 0;
    }

}
