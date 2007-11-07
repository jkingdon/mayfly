package net.sourceforge.mayfly.evaluation.expression;

public class RealTimeSource extends TimeSource {

    @Override
    public long current() {
        return System.currentTimeMillis();
    }

}
