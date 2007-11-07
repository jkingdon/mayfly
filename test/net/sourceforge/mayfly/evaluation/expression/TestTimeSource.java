package net.sourceforge.mayfly.evaluation.expression;

import org.joda.time.DateMidnight;

/**
 * @internal
 * Represents a broken clock, that is one that advances only
 * when told to advance.
 */
public class TestTimeSource extends TimeSource {

    private long simulatedTime = 0;

    @Override
    public long current() {
        return simulatedTime;
    }

    public void advanceTo(long simulatedTime) {
        if (this.simulatedTime >= simulatedTime) {
            throw new RuntimeException(
                "this clock isn't built to go backwards, or stay still, " +
                "when advance is called");
        }
        this.simulatedTime = simulatedTime;
    }

    /**
     * @internal
     * advance to a timestamp in the middle of the specified month
     * (this way timezones won't affect which month comes back).
     */
    public void advanceTo(int year, int month) {
        advanceTo(new DateMidnight(year, month, 15).getMillis());
    }
    
}
