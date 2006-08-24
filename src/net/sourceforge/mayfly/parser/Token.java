package net.sourceforge.mayfly.parser;

public class Token {

    private final TokenType type;
    private final String text;
    private final int startLineNumber;
    private final int startColumn;
    private final int endLineNumber;
    private final int endColumn;

    public Token(TokenType type, String text, Token oldToken) {
        this(type, text, 
            oldToken.startLineNumber(), oldToken.startColumn(), 
            oldToken.endLineNumber(), oldToken.endColumn());
    }

    public Token(TokenType type, String text, 
        int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
        this.type = type;
        this.text = text;
        this.startLineNumber = startLineNumber;
        this.startColumn = startColumn;
        this.endLineNumber = endLineNumber;
        this.endColumn = endColumn;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int startLineNumber() {
        return startLineNumber;
    }

    public int startColumn() {
        return startColumn;
    }

    public int endLineNumber() {
        return endLineNumber;
    }

    public int endColumn() {
        return endColumn;
    }

}
