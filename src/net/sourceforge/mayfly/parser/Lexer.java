package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.util.ImmutableByteArray;
import net.sourceforge.mayfly.util.StringBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private static final int END_OF_FILE_CHARACTER = -1;

    private final Reader sql;
    private int currentLine;
    private int currentColumn;
    private int previousLine = -1;
    private int previousColumn = -1;
    private int tokenLine;
    private int tokenColumn;
    
    /**
     * Command, if we are just lexing a single command.
     */
    private final String command;

    /*
     * Start machinery used to keep track of commands if we are lexing from
     * a Reader.
     */
    private StringBuilder currentCommand;
    private List commands;
    private List commandLocations;
    private int commandLine;
    private int commandColumn;
    // End command-tracking machinery

    private int current;
    private List tokens;

    public Lexer(String sql) {
        this(new StringReader(sql), sql);
    }
    
    /**
     * Create a lexer which reads input from a Reader.
     * The caller is responsible for closing the Reader.
     */
    public Lexer(Reader sql) {
        this(sql, null);
        this.commands = new ArrayList();
        this.commandLocations = new ArrayList();
    }

    public Lexer(Reader sql, String command) {
        this.sql = sql;
        this.command = command;
        this.currentLine = 1;
        this.currentColumn = 1;
    }
    
    Lexer() {
        this((Reader)null);
    }

    public List tokens() {
        startCommand();
        List tokens = lex();
        if (commands == null) {
            return tokens;
        }
        else {
            return attachCommandsToEachToken(tokens);
        }
    }

    private List attachCommandsToEachToken(List tokens) {
        List result = new ArrayList();
        for (int i = 0; i < tokens.size(); ++i) {
            Token token = (Token) tokens.get(i);
            result.add(
                token.withCommand(
                    locationToCommand(
                        token.startLineNumber(), token.startColumn())));
        }
        return result;
    }

    private List lex() {
        tokens = new ArrayList();
        current = nextCharacter();
        markTokenStart();
        while (true) {
            if (current == '.') {
                current = nextCharacter();
                addToken(tokens, TokenType.PERIOD, ".");
            }
            else if (current == ';') {
                endOfCommand();
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
                lexIdentifierOrHex();
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
                endOfCommand();
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

    private void lexIdentifierOrHex() {
        StringBuilder textBuilder = new StringBuilder();
        
        if (current == 'x' || current == 'X') {
            textBuilder.append((char)current);
            current = nextCharacter();
            if (current == '\'') {
                // hex constant

                current = nextCharacter();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                while (current != '\'') {
                    int first = parseHex(current);
                    current = nextCharacter();
                    if (current == '\'') {
                        throw new MayflyException(
                            "hex constant must have an even number of digits",
                            currentCharacter());
                    }
                    int second = parseHex(current);
                    bytes.write(combineHexDigits(first, second));
                    current = nextCharacter();
                }
                current = nextCharacter();
                Token newToken = new BinaryToken(
                    new ImmutableByteArray(bytes.toByteArray()), 
                    tokenLocation());
                addToken(tokens, newToken);
                return;
            }
        }

        while (isIdentifierCharacter(current)) {
            textBuilder.append((char)current);
            current = nextCharacter();
        }
        String text = textBuilder.toString();
        addToken(tokens, keywordOrIdentifier(text), text);
    }

    int combineHexDigits(int first, int second) {
        return (first << 4) + second;
    }

    int parseHex(int character) {
        if (character >= '0' && character <= '9') {
            return character - '0';
        }
        if (character >= 'a' && character <= 'f') {
            return character - 'a' + 10;
        }
        if (character >= 'A' && character <= 'F') {
            return character - 'A' + 10;
        }
        else {
            /* Not desirable to give the whole string constant, as it
               might be quite long.
             */
            throw new MayflyException(
                "invalid character " + describeCharacter(character) + 
                " in hex constant",
                currentCharacter());
        }
    }

    private Location currentCharacter() {
        return new Location(previousLine, previousColumn, 
            currentLine, currentColumn, command);
    }

    /**
     * Usage is to call nextCharacter() and then call this
     * method.  In other words, the character most recently
     * read by nextCharacter is <i>not</i> part of the token
     * we are adding here; the character before that is the
     * last character of the token.
     */
    private void addToken(List tokens, TokenType tokenType, String text) {
        TextToken newToken = new TextToken(tokenType, text, tokenLocation());
        addToken(tokens, newToken);
    }

    private void addToken(List tokens, Token newToken) {
        tokens.add(newToken);
        markTokenStart();
    }

    private void markTokenStart() {
        tokenLine = previousLine;
        tokenColumn = previousColumn;
    }

    private Location tokenLocation() {
        return new Location(tokenLine, tokenColumn, previousLine, previousColumn,
            command);
    }
    
    /**
     * Key difference with {@link #addToken(List, TokenType, String)}
     * is that we haven't called nextCharacter (since we are at the
     * end of file).
     */
    private void addEndOfFile(List tokens) {
        tokens.add(
            new EndOfFileToken(previousLine, previousColumn, command)
        );
    }

    String describeCharacter(int current) {
        if (current == -1) {
            return "end of file";
        }
        else if (current >= 0 && current <= 0x1f || current >= 0x7f && current <= 0xa0) {
            return "0x" + Integer.toHexString(current);
        }
        else if (current == '\'') {
            return "single quote";
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
            if (character != -1) {
                if (commands != null) {
                    currentCommand.append((char)character);
                }
            }

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

    public int commandCount() {
        if (commands.size() != commandLocations.size()) {
            throw new MayflyInternalException("confused about command tracking");
        }
        return commands.size();
    }

    public String command(int index) {
        return (String) commands.get(index);
    }

    public String locationToCommand(int line, int column) {
        for (int i = 0; i < commandCount(); ++i) {
            Location location = (Location) commandLocations.get(i);
            if (location.contains(line, column)) {
                return command(i);
            }
        }
        return null;
    }

    private void endOfCommand() {
        if (commands != null) {
            if (currentCommand.length() == 0) {
                commands.add(currentCommand.toString());
            }
            else {
                int lastCharacter = currentCommand.length() - 1;
                if (currentCommand.charAt(lastCharacter) == ';') {
                    currentCommand.delete(lastCharacter, lastCharacter + 1);
                }
    
                commands.add(currentCommand.toString());
            }
            
            commandLocations.add(new Location(
                commandLine, commandColumn, previousLine, previousColumn));
            startCommand();
        }
    }

    private void startCommand() {
        currentCommand = new StringBuilder();
        commandLine = currentLine;
        commandColumn = currentColumn;
    }

}
