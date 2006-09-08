package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.util.ImmutableByteArray;

abstract public class Token {

    private final TokenType type;
    private final int startLineNumber;
    private final int startColumn;
    private final int endLineNumber;
    private final int endColumn;

    protected Token(TokenType type, Token oldToken) {
        this(type, oldToken.startLineNumber(), 
            oldToken.startColumn(), oldToken.endLineNumber(), 
            oldToken.endColumn());
    }

    protected Token(TokenType type, int startLineNumber, 
        int startColumn, int endLineNumber, int endColumn) {
        this.type = type;
        this.startLineNumber = startLineNumber;
        this.startColumn = startColumn;
        this.endLineNumber = endLineNumber;
        this.endColumn = endColumn;
    }

    public TokenType getType() {
        return type;
    }

    public ImmutableByteArray getBytes() {
        throw new MayflyInternalException(
            "Cannot get bytes for token of type " + type.description());
    }

    public String getText() {
        throw new MayflyInternalException(
            "Cannot get text for token of type " + type.description());
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

    String describe() {
        TokenType type = getType();
        if (type == TokenType.NUMBER) {
            return getText();
        }
        else if (type == TokenType.IDENTIFIER) {
            return getText();
        }
        else {
            return type.description();
        }
    }

}
