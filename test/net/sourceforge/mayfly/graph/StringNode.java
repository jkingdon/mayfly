package net.sourceforge.mayfly.graph;

public class StringNode extends Node {

    private final String string;

    public StringNode(String string) {
        this.string = string;
    }

    public int backupOrdering(Node other) {
        StringNode stringNode = (StringNode) other;
        return string.compareTo(stringNode.string);
    }

    public String toString() {
        return string;
    }

}
