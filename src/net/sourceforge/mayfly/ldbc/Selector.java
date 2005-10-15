package net.sourceforge.mayfly.ldbc;

public interface Selector<T> {
    public boolean evaluate(T candidate);
}
