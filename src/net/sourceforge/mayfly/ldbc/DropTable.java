package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.ldbc.Tree.*;

public class DropTable extends Command {

    public static DropTable dropTableFromTree(Tree tree) {
        Children children = tree.children();
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

}
