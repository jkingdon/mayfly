package net.sourceforge.mayfly.ldbc;

public interface Transformer<From, To> {
    To transform(From from);
}
