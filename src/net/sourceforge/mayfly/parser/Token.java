package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.util.ImmutableByteArray;

abstract public class Token {

    public final TokenType type;
    public final Location location;

    protected Token(TokenType type, Token oldToken) {
        this(type, oldToken.location);
    }

    protected Token(TokenType type, Location location) {
        this.type = type;
        this.location = location;
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
        return location.startLineNumber;
    }

    public int startColumn() {
        return location.startColumn;
    }

    public int endLineNumber() {
        return location.endLineNumber;
    }

    public int endColumn() {
        return location.endColumn;
    }
    
    String describe() {
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

    abstract public Token withCommand(String command);

}
