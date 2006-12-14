package net.sourceforge.mayfly.graph;

public abstract class Node {

    /**
     * @internal
     * Called the "backup" ordering because it only is used
     * to select nodes when the graph does not impose one
     * order or the other.
     * 
     * However, it should only return 0 if the objects are
     * the same.
     * 
     * Feel free to cast other to the type which you know
     * all nodes in this graph to be.
     * 
     * @return -1,0,1 as with comparators.
     */
    abstract public int backupOrdering(Node other);

}
