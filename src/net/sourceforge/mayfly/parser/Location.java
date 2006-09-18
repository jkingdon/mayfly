package net.sourceforge.mayfly.parser;

public class Location {

    public static final Location UNKNOWN = new Location(-1, -1, -1, -1);

    public final int startLineNumber;
    public final int startColumn;
    public final int endLineNumber;
    public final int endColumn;

    public Location(int startLineNumber, int startColumn, 
        int endLineNumber, int endColumn) {
        this.startLineNumber = startLineNumber;
        this.startColumn = startColumn;
        this.endLineNumber = endLineNumber;
        this.endColumn = endColumn;
    }

    public Location combine(Location right) {
        return new Location(
            startLineNumber, startColumn, right.endLineNumber, right.endColumn);
    }

}
