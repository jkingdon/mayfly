package net.sourceforge.mayfly.graph;

public class StringNode extends Node {

    private final String string;

    public StringNode(String string) {
        this.string = string;
    }

    @Override
    public int backupOrdering(Node other) {
        StringNode stringNode = (StringNode) other;
        return string.compareTo(stringNode.string);
    }

    @Override
    public String toString() {
        return string;
    }

}
