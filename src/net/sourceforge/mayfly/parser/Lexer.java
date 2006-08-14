package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.StringBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private Reader sql;

    public Lexer(String sql) {
        this(new StringReader(sql));
    }
    
    /**
     * Create a lexer which reads input from a Reader.
     * The caller is responsible for closing the Reader.
     */
    public Lexer(Reader sql) {
        this.sql = sql;
    }
    
    Lexer() {
        this((Reader)null);
    }

    public List tokens() {
        List tokens = new ArrayList();
        int current = nextCharacter();
        while (true) {
            if (current == '.') {
                tokens.add(new Token(TokenType.PERIOD, "."));
                current = nextCharacter();
            }
            else if (current == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, ";"));
                current = nextCharacter();
            }
            else if (current == ',') {
                tokens.add(new Token(TokenType.COMMA, ","));
                current = nextCharacter();
            }
            else if (current == '+') {
                tokens.add(new Token(TokenType.PLUS, "+"));
                current = nextCharacter();
            }
            else if (current == '-') {
                tokens.add(new Token(TokenType.MINUS, "-"));
                current = nextCharacter();
            }
            else if (current == '/') {
                tokens.add(new Token(TokenType.DIVIDE, "/"));
                current = nextCharacter();
            }
            else if (current == '*') {
                tokens.add(new Token(TokenType.ASTERISK, "*"));
                current = nextCharacter();
            }
            else if (current == '(') {
                tokens.add(new Token(TokenType.OPEN_PAREN, "("));
                current = nextCharacter();
            }
            else if (current == ')') {
                tokens.add(new Token(TokenType.CLOSE_PAREN, ")"));
                current = nextCharacter();
            }
            else if (current == '?') {
                tokens.add(new Token(TokenType.PARAMETER, "?"));
                current = nextCharacter();
            }
            else if (current == '<') {
                current = nextCharacter();
                if (current == '>') {
                    tokens.add(new Token(TokenType.LESS_GREATER, "<>"));
                    current = nextCharacter();
                }
                else if (current == '=') {
                    tokens.add(new Token(TokenType.LESS_EQUAL, "<="));
                    current = nextCharacter();
                }
                else {
                    tokens.add(new Token(TokenType.LESS, "<"));
                }
            }
            else if (current == '>') {
                current = nextCharacter();
                if (current == '=') {
                    tokens.add(new Token(TokenType.GREATER_EQUAL, ">="));
                    current = nextCharacter();
                }
                else {
                    tokens.add(new Token(TokenType.GREATER, ">"));
                }
            }
            else if (current == '=') {
                current = nextCharacter();
                tokens.add(new Token(TokenType.EQUAL, "="));
            }
            else if (current == '!') {
                current = nextCharacter();
                if (current == '=') {
                    tokens.add(new Token(TokenType.BANG_EQUAL, "!="));
                    current = nextCharacter();
                }
                else {
                    throw new MayflyException("expected '=' but got " + describeCharacter(current));
                }
            }
            else if (current == '|') {
                current = nextCharacter();
                if (current == '|') {
                    tokens.add(new Token(TokenType.CONCATENATE, "||"));
                    current = nextCharacter();
                }
                else {
                    throw new MayflyException("expected '|' but got " + describeCharacter(current));
                }
            }
            else if (isIdentifierStart(current)) {
                StringBuilder text = new StringBuilder();
                while (isIdentifierCharacter(current)) {
                    text.append((char)current);
                    current = nextCharacter();
                }
                tokens.add(keywordOrIdentifier(text.toString()));
            }
            else if (current >= '0' && current <= '9') {
                StringBuilder text = new StringBuilder();
                while (current >= '0' && current <= '9') {
                    text.append((char)current);
                    current = nextCharacter();
                }
                tokens.add(new Token(TokenType.NUMBER, text.toString()));
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
                tokens.add(new Token(TokenType.IDENTIFIER, text.toString()));
            }
            else if (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
                current = nextCharacter();
            }
            else if (current == -1) {
                tokens.add(new Token(TokenType.END_OF_FILE));
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
                tokens.add(new Token(TokenType.QUOTED_STRING, text.toString()));
            }
            else {
                throw new MayflyException("unexpected character " + describeCharacter(current));
            }
        }
        return tokens;
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

    private Token keywordOrIdentifier(String text) {
        TokenType type = TokenType.lookupKeyword(text);
        if (type != null) {
            return new Token(type, text);
        }
        else {
            return new Token(TokenType.IDENTIFIER, text);
        }
    }

    private int nextCharacter() {
        try {
            return sql.read();
        } catch (IOException e) {
            throw new MayflyException(e);
        }
    }

    private boolean isIdentifierStart(int current) {
        return (current >= 'a' && current <= 'z') || (current >= 'A' && current <= 'Z');
    }

    private boolean isIdentifierCharacter(int current) {
        return isIdentifierStart(current) || (current >= '0' && current <= '9') || current == '_';
    }

}
