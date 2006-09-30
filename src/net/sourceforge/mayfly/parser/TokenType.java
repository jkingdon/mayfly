package net.sourceforge.mayfly.parser;

import java.util.HashMap;
import java.util.Map;

public class TokenType {
    
    private static Map keywords = new HashMap();

    private static TokenType registerKeyword(String lowercaseKeyword) {
        TokenType type = new TokenType(lowercaseKeyword.toUpperCase());
        keywords.put(lowercaseKeyword, type);
        return type;
    }

    public static final TokenType END_OF_FILE = new TokenType("end of file");
    public static final TokenType BINARY = new TokenType("binary data");

    public static final TokenType IDENTIFIER = new TokenType("identifier");
    public static final TokenType QUOTED_STRING = new TokenType("string");
    public static final TokenType NUMBER = new TokenType("number");
    public static final TokenType PERIOD = new TokenType("'.'");
    public static final TokenType COMMA = new TokenType("','");
    public static final TokenType CONCATENATE = new TokenType("||");
    public static final TokenType PLUS = new TokenType("'+'");
    public static final TokenType MINUS = new TokenType("'-'");
    public static final TokenType DIVIDE = new TokenType("'/'");
    public static final TokenType ASTERISK = new TokenType("'*'");
    public static final TokenType OPEN_PAREN = new TokenType("'('");
    public static final TokenType CLOSE_PAREN = new TokenType("')'");
    public static final TokenType PARAMETER = new TokenType("'?'");
    public static final TokenType LESS = new TokenType("'<'");
    public static final TokenType LESS_EQUAL = new TokenType("'<='");
    public static final TokenType GREATER = new TokenType("'>'");
    public static final TokenType GREATER_EQUAL = new TokenType("'>='");
    public static final TokenType EQUAL = new TokenType("'='");
    public static final TokenType LESS_GREATER = new TokenType("'<>'");
    public static final TokenType BANG_EQUAL = new TokenType("'!='");
    public static final TokenType SEMICOLON = new TokenType("';'");

    public static final TokenType KEYWORD_add = registerKeyword("add");
    public static final TokenType KEYWORD_all = registerKeyword("all");
    public static final TokenType KEYWORD_alter = registerKeyword("alter");
    public static final TokenType KEYWORD_and = registerKeyword("and");
    public static final TokenType KEYWORD_asc = registerKeyword("asc");
    public static final TokenType KEYWORD_authorization = registerKeyword("authorization");
    public static final TokenType KEYWORD_avg = registerKeyword("avg");
    public static final TokenType KEYWORD_by = registerKeyword("by");
    public static final TokenType KEYWORD_character = registerKeyword("character");
    public static final TokenType KEYWORD_column = registerKeyword("column");
    public static final TokenType KEYWORD_constraint = registerKeyword("constraint");
    public static final TokenType KEYWORD_count = registerKeyword("count");
    public static final TokenType KEYWORD_create = registerKeyword("create");
    public static final TokenType KEYWORD_cross = registerKeyword("cross");
    public static final TokenType KEYWORD_current_timestamp = 
        registerKeyword("current_timestamp");
    public static final TokenType KEYWORD_decimal = registerKeyword("decimal");
    public static final TokenType KEYWORD_default = registerKeyword("default");
    public static final TokenType KEYWORD_delete = registerKeyword("delete");
    public static final TokenType KEYWORD_desc = registerKeyword("desc");
    public static final TokenType KEYWORD_distinct = registerKeyword("distinct");
    public static final TokenType KEYWORD_drop = registerKeyword("drop");
    public static final TokenType KEYWORD_exists = registerKeyword("exists");
    public static final TokenType KEYWORD_foreign = registerKeyword("foreign");
    public static final TokenType KEYWORD_from = registerKeyword("from");
    public static final TokenType KEYWORD_group = registerKeyword("group");
    public static final TokenType KEYWORD_having = registerKeyword("having");
    public static final TokenType KEYWORD_if = registerKeyword("if");
    public static final TokenType KEYWORD_in = registerKeyword("in");
    public static final TokenType KEYWORD_inner = registerKeyword("inner");
    public static final TokenType KEYWORD_insert = registerKeyword("insert");
    public static final TokenType KEYWORD_int = registerKeyword("int");
    public static final TokenType KEYWORD_integer = registerKeyword("integer");
    public static final TokenType KEYWORD_into = registerKeyword("into");
    public static final TokenType KEYWORD_is = registerKeyword("is");
    public static final TokenType KEYWORD_join = registerKeyword("join");
    public static final TokenType KEYWORD_key = registerKeyword("key");
    public static final TokenType KEYWORD_left = registerKeyword("left");
    public static final TokenType KEYWORD_limit = registerKeyword("limit");
    public static final TokenType KEYWORD_max = registerKeyword("max");
    public static final TokenType KEYWORD_min = registerKeyword("min");
    public static final TokenType KEYWORD_not = registerKeyword("not");
    public static final TokenType KEYWORD_null = registerKeyword("null");
    public static final TokenType KEYWORD_on = registerKeyword("on");
    public static final TokenType KEYWORD_or = registerKeyword("or");
    public static final TokenType KEYWORD_order = registerKeyword("order");
    public static final TokenType KEYWORD_outer = registerKeyword("outer");
    public static final TokenType KEYWORD_primary = registerKeyword("primary");
    public static final TokenType KEYWORD_references = registerKeyword("references");
    public static final TokenType KEYWORD_schema = registerKeyword("schema");
    public static final TokenType KEYWORD_select = registerKeyword("select");
    public static final TokenType KEYWORD_set = registerKeyword("set");
    public static final TokenType KEYWORD_smallint = registerKeyword("smallint");
    public static final TokenType KEYWORD_sum = registerKeyword("sum");
    public static final TokenType KEYWORD_table = registerKeyword("table");
    public static final TokenType KEYWORD_unique = registerKeyword("unique");
    public static final TokenType KEYWORD_update = registerKeyword("update");
    public static final TokenType KEYWORD_values = registerKeyword("values");
    public static final TokenType KEYWORD_varchar = registerKeyword("varchar");
    public static final TokenType KEYWORD_where = registerKeyword("where");

    public static TokenType lookupKeyword(String text) {
        return (TokenType) keywords.get(text.toLowerCase());
    }

    private final String description;

    public TokenType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
    
    public String toString() {
        return description();
    }

}
