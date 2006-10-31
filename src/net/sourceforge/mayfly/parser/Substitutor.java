package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.util.ImmutableByteArray;

import org.apache.commons.lang.StringEscapeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Substitutor {

    public static List substitute(List tokens, List parameters) {
        List result = new ArrayList();
        Iterator parameterIterator = parameters.iterator();
        for (Iterator tokenIterator = tokens.iterator(); tokenIterator.hasNext();) {
            Token token = (Token) tokenIterator.next();
            if (token.getType() == TokenType.PARAMETER) {
                Object value = parameterIterator.next();
                result.addAll(tokensFromValue(value, token));
            }
            else {
                result.add(token);
            }
        }
        return result;
    }
    
    public static int parameterCount(List tokens) {
        int count = 0;
        for (Iterator iter = tokens.iterator(); iter.hasNext();) {
            Token token = (Token) iter.next();
            if (token.getType() == TokenType.PARAMETER) {
                ++count;
            }
        }
        return count;
    }

    private static Token tokenFromValue(Object value, Token oldToken) {
        if (value instanceof Number) {
            Number numberValue = (Number) value;
            return new TextToken(TokenType.NUMBER, numberValue.toString(), oldToken);
        }
        else if (value instanceof String) {
            String stringValue = (String) value;
            return new TextToken(TokenType.QUOTED_STRING, 
                "'" + StringEscapeUtils.escapeSql(stringValue) + "'",
                oldToken);
        }
        else if (value instanceof ImmutableByteArray) {
            ImmutableByteArray binaryValue = (ImmutableByteArray) value;
            return new BinaryToken(binaryValue, oldToken);
        }
        else if (value == null) {
            return new TextToken(TokenType.KEYWORD_null, "null", oldToken);
        }
        else {
            throw new MayflyInternalException(
                "Don't know how to substitute a " + value.getClass().getName());
        }
    }

    private static List tokensFromValue(Object value, Token oldToken) {
        if (value instanceof BigDecimal) {
            /*
               Here we pay the price for wanting to be able to someday
               do MySQL-style token-pasting games.  If a parameter
               were like in (most) other SQL dialects, we'd be doing
               the whole thing at the Literal level instead, and avoiding
               this silliness.
             */
            List result = new ArrayList();
            BigDecimal decimal = (BigDecimal) value;
            if (decimal.signum() == -1) {
                result.add(new TextToken(TokenType.MINUS, "-", oldToken));
            }
            decimal = decimal.abs();
            BigInteger integerPart = decimal.toBigInteger();
            result.add(new TextToken(TokenType.NUMBER, 
                integerPart.toString(), oldToken));

            BigDecimal fractionalPart = decimal.subtract(
                new BigDecimal(integerPart.toString()));
            result.add(new TextToken(TokenType.PERIOD, ".", oldToken));
            String fractionalString = fractionalPart.toString();
            if (!fractionalString.startsWith("0.")) {
                throw new MayflyInternalException(
                    "Lost in number un-parsing, " +
                    "fractional string was " + fractionalString,
                    oldToken.location);
            }
            result.add(new TextToken(TokenType.NUMBER, 
                fractionalString.substring(2), oldToken));
            return Collections.unmodifiableList(result);
        }
        else {
            return Collections.singletonList(tokenFromValue(value, oldToken));
        }
        
    }
}
