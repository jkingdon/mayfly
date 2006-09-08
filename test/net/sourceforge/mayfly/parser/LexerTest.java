package net.sourceforge.mayfly.parser;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexerTest extends TestCase {
    
    public void testEmpty() throws Exception {
        check(new TokenType[] { TokenType.END_OF_FILE }, new String[] { null }, "");
    }

    public void testSingleCharacterTokens() throws Exception {
        check(
            new TokenType[] {
                TokenType.PERIOD,
                TokenType.PLUS,
                TokenType.MINUS,
                TokenType.OPEN_PAREN,
                TokenType.CLOSE_PAREN,
                TokenType.COMMA,
                TokenType.PARAMETER,
                TokenType.END_OF_FILE,
            },
            new String[] { ".", "+", "-", "(", ")", ",", "?", null},
            ".+-(),?"
        );
    }
    
    public void testSlashAndStar() throws Exception {
        // At least for now, we don't recognize /* */ style comments,
        // so this is simple
        check(
            new TokenType[] {
                TokenType.DIVIDE,
                TokenType.ASTERISK,
                TokenType.END_OF_FILE,
            },
            new String[] { "/", "*", null},
            "/ *"
        );
    }
    
    public void testSql92Comments() throws Exception {
        check(
            new TokenType[] {
                TokenType.MINUS,
                TokenType.MINUS,
                TokenType.MINUS,
                TokenType.END_OF_FILE
            },
            new String[] { "-", "-", "-", null },
            "- - ----- this is a comment\n" +
            "-- this is another comment\r\n" +
            "- --- and a third comment"
        );
    }
    
    public void testLessThanAndGreaterThan() throws Exception {
        check(
            new TokenType[] {
                TokenType.LESS,
                TokenType.LESS_GREATER,
                TokenType.LESS_EQUAL,
                TokenType.LESS,
                TokenType.PLUS,
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.END_OF_FILE
            },
            new String[] { "<", "<>", "<=", "<", "+", ">", ">=", null},
            "<<><=<+>>="
        );
    }
    
    public void testEquals() throws Exception {
        check(
            new TokenType[] {
                TokenType.EQUAL,
                TokenType.BANG_EQUAL,
                TokenType.EQUAL,
                TokenType.END_OF_FILE
            },
            new String[] { "=", "!=", "=", null},
            "=!=="
        );
    }
    
    public void testBangWithoutEqualsAtEnd() throws Exception {
        try {
            lex("!");
            fail();
        } catch (MayflyException e) {
            assertEquals("expected '=' but got end of file",
                e.getMessage());
        }
    }

    public void testBangWithoutEqualsInMiddle() throws Exception {
        try {
            lex("!!");
            fail();
        } catch (MayflyException e) {
            assertEquals("expected '=' but got '!'",
                e.getMessage());
        }
    }

    public void testVerticalBars() throws Exception {
        check(
            new TokenType[] {
                TokenType.CONCATENATE,
                TokenType.CONCATENATE,
                TokenType.END_OF_FILE
            },
            new String[] { "||", "||", null},
            "||||"
        );
    }

    public void testSingleVerticalBar() throws Exception {
        try {
            lex("|||");
            fail();
        } catch (MayflyException e) {
            assertEquals("expected '|' but got end of file",
                e.getMessage());
        }
    }

    
    public void testUnexpectedCharacter() throws Exception {
        try {
            lex("{");
            fail();
        } catch (MayflyException e) {
            assertEquals("unexpected character '{'", e.getMessage());
        }
    }

    public void testDescribeCharacter() throws Exception {
        try {
            lex("\u0000");
            fail();
        } catch (MayflyException e) {
            assertEquals("unexpected character 0x0", e.getMessage());
        }
        
        assertEquals("0x0", new Lexer().describeCharacter(0x0));
        assertEquals("0x1", new Lexer().describeCharacter(0x1));
        assertEquals("0x1f", new Lexer().describeCharacter(0x1f));
        assertEquals("' '", new Lexer().describeCharacter(0x20));
        assertEquals("'~'", new Lexer().describeCharacter(0x7e));
        assertEquals("0x7f", new Lexer().describeCharacter(0x7f));
        assertEquals("0x80", new Lexer().describeCharacter(0x80));
        assertEquals("0x9f", new Lexer().describeCharacter(0x9f));
        assertEquals("0xa0", new Lexer().describeCharacter(0xa0)); // non breaking space
        
        // I think our default behavior has to be to hope that whoever
        // is looking at the message (an IDE, a console, etc) has some
        // way of showing the character.  Certainly if it makes sense to put,
        // say, Russian text in string constants or quoted identifiers,
        // we shouldn't just
        // start displaying messages in hex if there is a quote character
        // missing or the like.
        
        // The flip side is that if the character doesn't show up in
        // a way which lets us identify which character it is, we
        // aren't being very informative.
        assertEquals("'\u00a1'", new Lexer().describeCharacter(0xa1)); // inverted exclamation point
        
        // What about other unicode characters which won't show up clearly (various kinds of spaces,
        // byte-order-mark, line/paragraph separator, etc)?
    }

    public void testIdentifierToEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "Foo", null },
            "Foo"
        );
    }

    public void testIdentifierToCharacter() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.COMMA, TokenType.END_OF_FILE },
            new String[] { "Foo", ",", null },
            "Foo,"
        );
    }

    public void testNumberToEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.NUMBER, TokenType.END_OF_FILE },
            new String[] { "42", null },
            "42"
        );
    }

    public void testNumberToCharacter() throws Exception {
        check(new TokenType[] { TokenType.NUMBER, TokenType.COMMA, TokenType.END_OF_FILE },
            new String[] { "42", ",", null },
            "42,"
        );
    }

    public void testIdentifierCharacters() throws Exception {
        check(new TokenType[] {
                TokenType.IDENTIFIER, TokenType.MINUS, TokenType.IDENTIFIER, TokenType.END_OF_FILE
            },
            new String[] { "abcdefghijklmnopqrstuvwxyz", "-", "ABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789", null },
            "abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789"
        );
    }

    public void testIdentifierToSpace() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "Foo", null },
            "Foo "
        );
    }

    public void testKeywords() throws Exception {
        check(new TokenType[] {
                TokenType.KEYWORD_all,
                TokenType.KEYWORD_and,
                TokenType.END_OF_FILE
            },
            new String[] {
                "ALL",
                "AND",
                null
            },
            "ALL AND"
        );
    }
    
    public void testIdentifierStartsWithKeyword() throws Exception {
        check(new TokenType[] {
                TokenType.IDENTIFIER,
                TokenType.KEYWORD_int,
                TokenType.KEYWORD_integer,
                TokenType.END_OF_FILE
            },
            new String[] {
                "integrate",
                "int",
                "integer",
                null
            },
            "integrate int integer"
        );
    }
    
    public void testKeywordsAreCaseInsensitive() throws Exception {
        check(new TokenType[] { TokenType.KEYWORD_all, TokenType.END_OF_FILE },
            new String[] { "aLL", null },
            "aLL"
        );
    }

    public void testQuotedIdentifier() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { " !#$%^&*',+-/*", null },
            " \" !#$%^&*',+-/*\" "
        );
    }

    public void testQuotedIdentifierEndsAtEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "foo", null },
            "\"foo\""
        );
    }

    public void testUnterminatedQuotedIdentifier() throws Exception {
        try {
            lex(" \"foo ");
            fail();
        } catch (MayflyException e) {
            assertEquals("unterminated quoted identifier", e.getMessage());
        }
    }

    public void testString() throws Exception {
        check(new TokenType[] { 
                TokenType.QUOTED_STRING, 
                TokenType.QUOTED_STRING, 
                TokenType.COMMA, 
                TokenType.QUOTED_STRING, 
                TokenType.END_OF_FILE
            },
            new String[] { "'don''t'", "'a'''", ",", "'b''''c'", null },
            " 'don''t' 'a''','b''''c'"
        );
    }

    public void testStringEndsAtEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.QUOTED_STRING, TokenType.END_OF_FILE },
            new String[] { "'he said \"hi\"'", null },
            "'he said \"hi\"'"
        );
    }

    public void testUnterminatedString() throws Exception {
        try {
            lex(" 'foo ");
            fail();
        } catch (MayflyException e) {
            assertEquals("unterminated string", e.getMessage());
        }
    }

    public void testUnterminatedStringAfterEscape() throws Exception {
        try {
            lex(" 'foo''");
            fail();
        } catch (MayflyException e) {
            assertEquals("unterminated string", e.getMessage());
        }
    }

    public void testSpace() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "Foo", "BAR", null },
            "\tFoo BAR\n\r\n\n"
        );
    }
    
    public void testLineNumbers() throws Exception {
        List tokens = lex("width \n 53");
        assertEquals(3, tokens.size());

        {
            Token width = (Token) tokens.get(0);
            assertEquals("width", width.getText());
            assertEquals(1, width.startLineNumber());
            assertEquals(1, width.startColumn());
            assertEquals(1, width.endLineNumber());
            assertEquals(6, width.endColumn());
        }

        {
            Token fiftyThree = (Token) tokens.get(1);
            assertEquals("53", fiftyThree.getText());
            assertEquals(2, fiftyThree.startLineNumber());
            assertEquals(2, fiftyThree.startColumn());
            assertEquals(2, fiftyThree.endLineNumber());
            assertEquals(4, fiftyThree.endColumn());
        }
        
        {
            Token endOfFile = (Token) tokens.get(2);
            assertEquals(TokenType.END_OF_FILE, endOfFile.getType());
            assertEquals(2, endOfFile.startLineNumber());
            assertEquals(4, endOfFile.startColumn());
            assertEquals(2, endOfFile.endLineNumber());
            assertEquals(4, endOfFile.endColumn());
        }
    }
    
    public void testLineNumberAndComment() throws Exception {
        List tokens = lex("-- first line\n.-- comment at end of file");
        assertEquals(2, tokens.size());
        
        {
            Token period = (Token) tokens.get(0);
            assertEquals(TokenType.PERIOD, period.getType());
            assertEquals(2, period.startLineNumber());
            assertEquals(1, period.startColumn());
            assertEquals(2, period.endLineNumber());
            assertEquals(2, period.endColumn());
        }
        
        {
            Token endOfFile = (Token) tokens.get(1);
            assertEquals(TokenType.END_OF_FILE, endOfFile.getType());
            assertEquals(2, endOfFile.startLineNumber());
            assertEquals(27, endOfFile.startColumn());
            assertEquals(2, endOfFile.endLineNumber());
            assertEquals(27, endOfFile.endColumn());
        }
    }
    
    public void testNewlineEndsToken() throws Exception {
        List tokens = lex("'c''est'\n<");
        assertEquals(3, tokens.size());
        
        Token string = (Token) tokens.get(0);
        assertEquals(TokenType.QUOTED_STRING, string.getType());
        assertEquals(1, string.startLineNumber());
        assertEquals(1, string.startColumn());
        assertEquals(1, string.endLineNumber());
        assertEquals(9, string.endColumn());
    }
    
    private void check(TokenType[] expectedTypes, String[] expectedTexts, 
        String input) {
        check(expectedTypes, expectedTexts, lex(input));
    }

    public static void check(TokenType[] expectedTypes, String[] expectedTexts, 
        List actual) {
        assertEquals("test setup problem with expectations", 
            expectedTexts.length, expectedTypes.length);
        List actualTexts = new ArrayList(actual.size());
        List actualTypes = new ArrayList(actual.size());
        for (int i = 0; i < actual.size(); ++i) {
            Token token = (Token) actual.get(i);
            actualTypes.add(token.getType());
            if (token.getType() == TokenType.END_OF_FILE) {
                actualTexts.add(null);
            }
            else {
                actualTexts.add(token.getText());
            }
        }
        
        assertEquals(Arrays.asList(expectedTypes), actualTypes);
        assertEquals(Arrays.asList(expectedTexts), actualTexts);
    }
    
    private List lex(String sql) {
        return new Lexer(sql).tokens();
    }

}
