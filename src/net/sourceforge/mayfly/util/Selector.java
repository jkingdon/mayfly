package net.sourceforge.mayfly.util;

public interface Selector {
    public boolean evaluate(Object candidate);

    public static final Selector ALWAYS_TRUE =
        new Selector() {
            public boolean evaluate(Object candidate) {
                return true;
            }
        };

    public static final Selector ALWAYS_FALSE =
        new Selector() {
            public boolean evaluate(Object candidate) {
                return false;
            }
        };
}
