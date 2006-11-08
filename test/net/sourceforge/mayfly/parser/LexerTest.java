package net.sourceforge.mayfly.parser;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.MayflyAssert;

import java.io.StringReader;
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
        /*
            For the moment, I'm assuming that flagging an
            error on slash-star within a comment, as GCC
            does for C, would be too disruptive (we don't
            have any kind of warning capability).
            But it might detect some cases where someone
            comments out SQL which already has slash-star
            comments in it.
         */

        check(
            new TokenType[] {
                TokenType.DIVIDE,
                TokenType.ASTERISK,
                TokenType.END_OF_FILE,
            },
            new String[] { "/", "*", null},
            "/ *"
        );
        // /***/
        
        checkComment("a/**/b");
        checkComment("a/* * */b");
        checkComment("/* comments do not /* nest */ a b");
        checkComment(
            "a /* comment\n" +
            "with\n" +
            "newlines\n" +
            "*\n" +
            "*/b");
        checkComment("a/***/b");
        checkComment("a/****/b");

        checkUnclosed(" /*/ ", 1, 2, 1, 6);
        checkUnclosed("  /* unclosed comment", 1, 3, 1, 22);
        checkUnclosed("\n/* unclosed comment *", 2, 1, 2, 22);
    }

    private void checkComment(String string) {
        check(new TokenType[] {
            TokenType.IDENTIFIER, TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "a", "b", null },
            string
            );
    }
    
    private void checkUnclosed(String input, 
        int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
        try {
            lex(input);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("unclosed comment", e.getMessage());
            MayflyAssert.assertLocation(
                startLineNumber, startColumn, endLineNumber, endColumn, 
                e.location());
        }
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
            assertEquals(TokenType.END_OF_FILE, endOfFile.type);
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
            assertEquals(TokenType.PERIOD, period.type);
            MayflyAssert.assertLocation(2, 1, 2, 2, period.location);
        }
        
        {
            Token endOfFile = (Token) tokens.get(1);
            assertEquals(TokenType.END_OF_FILE, endOfFile.type);
            MayflyAssert.assertLocation(2, 27, 2, 27, endOfFile.location);
        }
    }
    
    public void testLineNumberAndSlashStarComment() throws Exception {
        List tokens = lex("/* first comment\n  *///* another comment */");
        assertEquals(2, tokens.size());
        
        {
            Token slash = (Token) tokens.get(0);
            assertEquals(TokenType.DIVIDE, slash.type);
            MayflyAssert.assertLocation(2, 5, 2, 6, slash.location);
        }
        
        {
            Token endOfFile = (Token) tokens.get(1);
            assertEquals(TokenType.END_OF_FILE, endOfFile.type);
            MayflyAssert.assertLocation(2, 27, 2, 27, endOfFile.location);
        }
    }
    
    public void testNewlineEndsToken() throws Exception {
        List tokens = lex("'c''est'\n<");
        assertEquals(3, tokens.size());
        
        Token string = (Token) tokens.get(0);
        assertEquals(TokenType.QUOTED_STRING, string.type);
        MayflyAssert.assertLocation(1, 1, 1, 9, string.location);
    }
    
    public void testColumnNumberWithNumberToken() throws Exception {
        List tokens = lex("(5");
        assertEquals(3, tokens.size());
        MayflyAssert.assertLocation(1, 1, 1, 2, ((Token)tokens.get(0)).location);
        MayflyAssert.assertLocation(1, 2, 1, 3, ((Token)tokens.get(1)).location);
    }
    
    public void testColumnNumberSingleCharacterTokens() throws Exception {
        List tokens = lex("((");
        assertEquals(3, tokens.size());
        MayflyAssert.assertLocation(1, 1, 1, 2, ((Token)tokens.get(0)).location);
        MayflyAssert.assertLocation(1, 2, 1, 3, ((Token)tokens.get(1)).location);
    }
    
    public void testCommandFinder() throws Exception {
        Lexer lexer = new Lexer(new StringReader(
            "insert into foo; drop table bar\n\n;   "));
        lexer.tokens();
        assertEquals(3, lexer.commandCount());
        assertEquals("insert into foo", lexer.command(0));
        assertEquals(" drop table bar\n\n", lexer.command(1));
        assertEquals("   ", lexer.command(2));
    }
    
    public void testCommandFinderAndEmptyStrings() throws Exception {
        Lexer lexer = new Lexer(new StringReader(
            ";;\n;drop table bar;"));
        lexer.tokens();
        assertEquals(5, lexer.commandCount());
        
        /* I don't think these empty commands will give rise to any 
           exceptions.  So I'm not sure it matters much whether they
           are "", or null, or what. */
        assertEquals("", lexer.command(0));
        assertEquals("", lexer.command(1));
        assertEquals("\n", lexer.command(2));
        assertEquals("drop table bar", lexer.command(3));
        assertEquals("", lexer.command(4));
    }
    
    public void testLocationToCommand() throws Exception {
        Lexer lexer = new Lexer(new StringReader(
            "insert into foo; drop table bar\n\n;   "));
        lexer.tokens();
        assertEquals("insert into foo", lexer.locationToCommand(1, 1));
        assertNull(lexer.locationToCommand(1, 16));
        assertEquals(" drop table bar\n\n", lexer.locationToCommand(1, 17));
        assertEquals(" drop table bar\n\n", lexer.locationToCommand(2, 1));
    }
    
    public void testTokensKnowCommands() throws Exception {
        Lexer lexer = new Lexer(new StringReader(
            "insert ; drop \n\n;   "));
        List tokens = lexer.tokens();
        assertEquals(5, tokens.size());
        assertEquals("insert ", ((Token)tokens.get(0)).location.command);
        assertNull(((Token)tokens.get(1)).location.command);
        assertEquals(" drop \n\n", ((Token)tokens.get(2)).location.command);
        assertNull(((Token)tokens.get(3)).location.command);
        assertNull(((Token)tokens.get(4)).location.command);
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
            actualTypes.add(token.type);
            if (token.type == TokenType.END_OF_FILE) {
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
