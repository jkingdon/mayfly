package net.sourceforge.mayfly.graph;

public class CycleDetectedException extends RuntimeException {
    
    public CycleDetectedException() {
        super("graph contains a cycle");
    }

}
