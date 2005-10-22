package net.sourceforge.mayfly.util;

public interface Transformer {
    Object transform(Object from);


    public static final Transformer JUST_RETURN =
        new Transformer() {
            public Object transform(Object from) {
                return from;
            }
        };
}
