package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.util.ImmutableList;

public class Limit {

    public static final int NO_OFFSET = 0;
    public static final int NO_LIMIT = Integer.MAX_VALUE;

    public static final Limit NONE = new Limit(NO_LIMIT, NO_OFFSET);

    private final int count;
    private final int offset;

    public Limit(int count, int offset) {
        this.count = count;
        this.offset = offset;
    }

    public ResultRows limit(ResultRows rows) {
        if (offset == 0 && count >= rows.size()) {
            return rows;
        }
        
        if (offset > rows.size()) {
            return new ResultRows();
        }
        int end = Math.min(offset + count, rows.size());
        return new ResultRows(new ImmutableList(rows.asList().subList(offset, end)));
    }

    public boolean isSpecified() {
        return offset != NO_OFFSET || count != NO_LIMIT;
    }

}
