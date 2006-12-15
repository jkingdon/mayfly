package net.sourceforge.mayfly.dump;

import net.sourceforge.mayfly.graph.Node;

public class TableNode extends Node {

    public final String name;

    public TableNode(String name) {
        this.name = name;
    }

    public int backupOrdering(Node other) {
        TableNode tableNode = (TableNode) other;
        return name.toUpperCase().compareTo(tableNode.name.toUpperCase());
    }

}
