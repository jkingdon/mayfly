package net.sourceforge.mayfly.evaluation.expression;

public class RealTimeSource extends TimeSource {

    public long current() {
        return System.currentTimeMillis();
    }

}
