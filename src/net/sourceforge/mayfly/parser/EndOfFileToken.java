package net.sourceforge.mayfly.parser;

public class EndOfFileToken extends Token {

    public EndOfFileToken(int line, int column, String command) {
        this(new Location(line, column, line, column, command));
    }
    
    private EndOfFileToken(Location location) {
        super(TokenType.END_OF_FILE, location);
    }

    public Token withCommand(String command) {
        return new EndOfFileToken(location.withCommand(command));
    }

}
