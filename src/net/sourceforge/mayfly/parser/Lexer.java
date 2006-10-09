package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.StringBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private static final int END_OF_FILE_CHARACTER = -1;
    private Reader sql;
    private int currentLine;
    private int currentColumn;
    private int previousLine = -1;
    private int previousColumn = -1;
    private int tokenLine;
    private int tokenColumn;

    public Lexer(String sql) {
        this(new StringReader(sql));
    }
    
    /**
     * Create a lexer which reads input from a Reader.
     * The caller is responsible for closing the Reader.
     */
    public Lexer(Reader sql) {
        this.sql = sql;
        this.currentLine = 1;
        this.currentColumn = 1;
    }
    
    Lexer() {
        this((Reader)null);
    }

    public List tokens() {
        List tokens = new ArrayList();
        int current = nextCharacter();
        markTokenStart();
        while (true) {
            if (current == '.') {
                current = nextCharacter();
                addToken(tokens, TokenType.PERIOD, ".");
            }
            else if (current == ';') {
                current = nextCharacter();
                addToken(tokens, TokenType.SEMICOLON, ";");
            }
            else if (current == ',') {
                current = nextCharacter();
                addToken(tokens, TokenType.COMMA, ",");
            }
            else if (current == '+') {
                current = nextCharacter();
                addToken(tokens, TokenType.PLUS, "+");
            }
            else if (current == '-') {
                current = nextCharacter();
                if (current == '-') {
                    while (true) {
                        current = nextCharacter();
                        if (current == '\n') {
                            current = nextCharacter();
                            markTokenStart();
                            break;
                        }
                        else if (current == END_OF_FILE_CHARACTER) {
                            markTokenStart();
                            break;
                        }
                    }
                }
                else {
                    addToken(tokens, TokenType.MINUS, "-");
                }
            }
            else if (current == '/') {
                current = nextCharacter();
                if (current == '*') {
                    boolean gotStar = false;
                    while (true) {
                        current = nextCharacter();
                        if (current == '*') {
                            gotStar = true;
                        }
                        else if (gotStar && current == '/') {
                            current = nextCharacter();
                            markTokenStart();
                            break;
                        }
                        else if (current == END_OF_FILE_CHARACTER) {
                            throw new MayflyException("unclosed comment",
                                tokenLocation());
                        }
                        else {
                            gotStar = false;
                        }
                    }
                }
                else {
                    addToken(tokens, TokenType.DIVIDE, "/");
                }
            }
            else if (current == '*') {
                current = nextCharacter();
                addToken(tokens, TokenType.ASTERISK, "*");
            }
            else if (current == '(') {
                current = nextCharacter();
                addToken(tokens, TokenType.OPEN_PAREN, "(");
            }
            else if (current == ')') {
                current = nextCharacter();
                addToken(tokens, TokenType.CLOSE_PAREN, ")");
            }
            else if (current == '?') {
                current = nextCharacter();
                addToken(tokens, TokenType.PARAMETER, "?");
            }
            else if (current == '<') {
                current = nextCharacter();
                if (current == '>') {
                    current = nextCharacter();
                    addToken(tokens, TokenType.LESS_GREATER, "<>");
                }
                else if (current == '=') {
                    current = nextCharacter();
                    addToken(tokens, TokenType.LESS_EQUAL, "<=");
                }
                else {
                    addToken(tokens, TokenType.LESS, "<");
                }
            }
            else if (current == '>') {
                current = nextCharacter();
                if (current == '=') {
                    current = nextCharacter();
                    addToken(tokens, TokenType.GREATER_EQUAL, ">=");
                }
                else {
                    addToken(tokens, TokenType.GREATER, ">");
                }
            }
            else if (current == '=') {
                current = nextCharacter();
                addToken(tokens, TokenType.EQUAL, "=");
            }
            else if (current == '!') {
                current = nextCharacter();
                if (current == '=') {
                    current = nextCharacter();
                    addToken(tokens, TokenType.BANG_EQUAL, "!=");
                }
                else {
                    throw new MayflyException("expected '=' but got " + describeCharacter(current));
                }
            }
            else if (current == '|') {
                current = nextCharacter();
                if (current == '|') {
                    current = nextCharacter();
                    addToken(tokens, TokenType.CONCATENATE, "||");
                }
                else {
                    throw new MayflyException("expected '|' but got " + describeCharacter(current));
                }
            }
            else if (isIdentifierStart(current)) {
                StringBuilder textBuilder = new StringBuilder();
                while (isIdentifierCharacter(current)) {
                    textBuilder.append((char)current);
                    current = nextCharacter();
                }
                String text = textBuilder.toString();
                addToken(tokens, keywordOrIdentifier(text), text);
            }
            else if (current >= '0' && current <= '9') {
                StringBuilder text = new StringBuilder();
                while (current >= '0' && current <= '9') {
                    text.append((char)current);
                    current = nextCharacter();
                }
                addToken(tokens, TokenType.NUMBER, text.toString());
            }
            else if (current == '\"') {
                StringBuilder text = new StringBuilder();
                current = nextCharacter();
                while (current != '\"') {
                    text.append((char)current);
                    current = nextCharacter();
                    if (current == -1) {
                        throw new MayflyException("unterminated quoted identifier");
                    }
                }
                current = nextCharacter();
                addToken(tokens, TokenType.IDENTIFIER, text.toString());
            }
            else if (current == ' ' || current == '\t' || current == '\n' || 
                current == '\r') {
                current = nextCharacter();
                markTokenStart();
            }
            else if (current == -1) {
                addEndOfFile(tokens);
                break;
            }
            else if (current == '\'') {
                StringBuilder text = new StringBuilder();
                text.append("'");
                current = nextCharacter();
                while (true) {
                    if (current == -1) {
                        throw new MayflyException("unterminated string");
                    }
                    if (current == '\'') {
                        current = nextCharacter();
                        if (current == '\'') {
                            current = nextCharacter();
                            text.append("''");
                        }
                        else {
                            break;
                        }
                    }
                    else {
                        text.append((char)current);
                        current = nextCharacter();
                    }
                }
                text.append("'");
                addToken(tokens, TokenType.QUOTED_STRING, text.toString());
            }
            else {
                throw new MayflyException("unexpected character " + describeCharacter(current));
            }
        }
        return tokens;
    }

    /**
     * Usage is to call nextCharacter() and then call this
     * method.  In other words, the character most recently
     * read by nextCharacter is <i>not</i> part of the token
     * we are adding here; the character before that is the
     * last character of the token.
     */
    private void addToken(List tokens, TokenType tokenType, String text) {
        tokens.add(
            new TextToken(tokenType, text, tokenLocation())
        );
        markTokenStart();
    }

    private void markTokenStart() {
        tokenLine = previousLine;
        tokenColumn = previousColumn;
    }

    private Location tokenLocation() {
        return new Location(tokenLine, tokenColumn, previousLine, previousColumn);
    }
    
    /**
     * Key difference with {@link #addToken(List, TokenType, String)}
     * is that we haven't called nextCharacter (since we are at the
     * end of file).
     */
    private void addEndOfFile(List tokens) {
        tokens.add(
            new EndOfFileToken(previousLine, previousColumn)
        );
    }

    String describeCharacter(int current) {
        if (current == -1) {
            return "end of file";
        }
        else if (current >= 0 && current <= 0x1f || current >= 0x7f && current <= 0xa0) {
            return "0x" + Integer.toHexString(current);
        }
        // Doesn't work for surrogate pairs.
        return "'" + (char)current + "'";
    }

    private TokenType keywordOrIdentifier(String text) {
        TokenType type = TokenType.lookupKeyword(text);
        return type != null ? type : TokenType.IDENTIFIER;
    }

    private int nextCharacter() {
        try {
            previousLine = currentLine;
            previousColumn = currentColumn;

            int character = sql.read();

            if (character == '\n') {
                currentColumn = 1;
                ++currentLine;
            }
            else {
                ++currentColumn;
            }
            return character;
        } catch (IOException e) {
            throw new MayflyException(e);
        }
    }

    private boolean isIdentifierStart(int current) {
        return (current >= 'a' && current <= 'z') || 
            (current >= 'A' && current <= 'Z');
    }

    private boolean isIdentifierCharacter(int current) {
        return isIdentifierStart(current) || 
            (current >= '0' && current <= '9') ||
            current == '_';
    }

}
