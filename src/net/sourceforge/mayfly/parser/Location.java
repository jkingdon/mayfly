package net.sourceforge.mayfly.parser;


public class Location {

    public static final Location UNKNOWN = new Location(-1, -1, -1, -1, null);

    public final int startLineNumber;
    public final int startColumn;
    public final int endLineNumber;
    public final int endColumn;

    public final String command;

    public Location(int startLineNumber, int startColumn, 
        int endLineNumber, int endColumn,
        String command) {
        this.startLineNumber = startLineNumber;
        this.startColumn = startColumn;
        this.endLineNumber = endLineNumber;
        this.endColumn = endColumn;
        
        this.command = command;
    }

    public Location(int startLineNumber, int startColumn, 
        int endLineNumber, int endColumn) {
        this(startLineNumber, startColumn, endLineNumber, endColumn, null);
    }

    public Location combine(Location right) {
        return new Location(
            startLineNumber, startColumn, right.endLineNumber, right.endColumn,
            command);
    }

    public boolean knowStart() {
        return startLineNumber != -1 && startColumn != -1;
    }

    public boolean knowEnd() {
        return endLineNumber != -1 && endColumn != -1;
    }

    public boolean contains(int line, int column) {
        if (line < startLineNumber) {
            return false;
        }
        else if (line == startLineNumber && column < startColumn) {
            return false;
        }
        else if (line == endLineNumber && column >= endColumn) {
            return false;
        }
        else if (line > endLineNumber) {
            return false;
        }
        return true;
    }

    public Location withCommand(String newCommand) {
        return new Location(
            startLineNumber, startColumn, endLineNumber, endColumn, 
            newCommand);
    }

}
