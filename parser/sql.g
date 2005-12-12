//
// Mayfly SQL grammer
// Based on the grammer from LDBC ( http://ldbc.sourceforge.net/ )
//

header {
package net.sourceforge.mayfly.parser;
}

class SQLParser extends Parser;

options {
    exportVocab = SQL;
    k = 3;
    buildAST = true;
    defaultErrorHandler = false;
}

tokens {
    COLUMN_LIST;
    CREATE_TABLE;
    UPDATE;
    INSERT;
    SELECT;
    SELECT_ONE_COLUMN;
    DELETE;
    COLUMN_DEF;
    PRIMARY_KEY;
    CREATE_INDEX;
    DROP_TABLE;
    DROP_INDEX;
    UPDATE_SET;
    EXPRESSION;
    CONDITION;
    SELECTED_TABLE;
    SELECT_ITEM;
    GROUP_BY;
    ORDER_BY;
    ORDER_ITEM;
    LIMIT;
    NOT;
    BETWEEN;
    NOT_BETWEEN;
    LIKE;
    NOT_LIKE;
    IS_NULL;
    IS_NOT_NULL;
    NEGATIVE;
    NULL_INSERT;
    DECIMAL_VALUE;
    DATE;
    TIME;
    TIMESTAMP;
    BINARY;
    COLUMN;
    DATATYPE;
    AGGREGATE;
    COMMIT;
    ROLLBACK;
    TYPE_INT;
    TYPE_VARCHAR;
    TYPE_DECIMAL;
    TYPE_DATETIME;
    TYPE_BLOB;
    TYPE_CLOB;
    FOREIGN_KEY;
    SET_AUTOCOMMIT_TRUE;
    SET_AUTOCOMMIT_FALSE;
    GET_AUTOINCREMENT_KEY;
    ALTER_TABLE_DROP_CONSTRAINT;
    ALTER_TABLE_RENAME;
    ALTER_TABLE_ADD_COLUMN;
    TABLE_ASTERISK;
    JOIN;
}

condition:
    cond_or
    { #condition = #([CONDITION, "condition"], #condition); }
;

cond_or:
    cond_and ("or"^ cond_and)*
;

cond_and:
    cond_neg ("and"^ cond_neg)*
;

cond_neg:
    cond_bin
    | ( 
        ( "not"! cond_bin )
        { #cond_neg = #([NOT, "not"], #cond_neg); }
      )
;

cond_bin:
	cond_exists
	| cond_rel
;

cond_exists:
	( "exists"^ OPEN_PAREN! select CLOSE_PAREN! )
;

cond_rel:
    cond_exp
    ( ( "between"! cond_exp "and"! cond_exp )
        { #cond_rel = #([BETWEEN, "between"], #cond_rel); }
    | ( "not"! "between"! cond_exp "and"! cond_exp )
        { #cond_rel = #([NOT_BETWEEN, "not_between"], #cond_rel); }
    | ( "like"! cond_exp )
        { #cond_rel = #([LIKE, "like"], #cond_rel); }
    | ( "not"! "like"! cond_exp )
        { #cond_rel = #([NOT_LIKE, "not_like"], #cond_rel); }
    | ( "is"! "null"! )
        { #cond_rel = #([IS_NULL, "is_null"], #cond_rel); }
    | ( "is"! "not"! "null"! )
        { #cond_rel = #([IS_NOT_NULL, "is_not_null"], #cond_rel); }
    | ( EQUAL^ cond_exp )
    | ( BIGGER^ cond_exp )
    | ( SMALLER^ cond_exp )
    | ( NOT_EQUAL^ cond_exp )
    | ( NOT_EQUAL_2^ cond_exp )
    | ( BIGGER_EQUAL^ cond_exp )
    | ( SMALLER_EQUAL^ cond_exp )
    | ( "in"^ OPEN_PAREN! cond_exp (COMMA! cond_exp)* CLOSE_PAREN! )
    )*
;

cond_exp:
    cond_sum (VERTBARS^ cond_sum)*
;

cond_sum:
    cond_factor ((PLUS^ | MINUS^) cond_factor)*
;

cond_factor:
    cond_term ((ASTERISK^ | DIVIDE^) cond_term)*
;

cond_term:
    cond_end
    | ( MINUS! cond_end )
        { #cond_term = #([NEGATIVE, "negative"], #cond_term); }
;

cond_end:
    value_or_column 
    | ( OPEN_PAREN^ condition CLOSE_PAREN! )
    | function
    | aggregate
;

expression:
    expr_exp
    { #expression = #([EXPRESSION, "expression"], #expression); }
;

expr_exp:
    expr_sum (VERTBARS^ expr_sum)*
;

expr_sum:
    expr_factor ((PLUS^ | MINUS^) expr_factor)*
;

expr_factor:
    expr_term ((ASTERISK^ | DIVIDE^) expr_term)*
;

expr_term:
    expr_end
    | ( MINUS! expr_end )
        { #expr_term = #([NEGATIVE, "negative"], #expr_term); }
;

expr_end:
    value_or_column 
    | ( OPEN_PAREN^ expression CLOSE_PAREN! )
    | function
    | aggregate
;

statement:
    (
    select 
    | update 
    | delete 
    | insert
    | create_table
    | create_index
    | drop_table
    | drop_index
    | alter_table
    | commit
    | rollback
    | set_autocommit
    | get_autoincrement_key
    )
    EOF!
;

commit:
    "commit"!
    { #commit = #([COMMIT, "commit"], #commit); }
;

rollback:
    "rollback"!
    { #rollback = #([ROLLBACK, "rollback"], #rollback); }
;

get_autoincrement_key:
	"get"! "autoincrement"! "key"!
    { #get_autoincrement_key = #([GET_AUTOINCREMENT_KEY, "get_autoincrement_key"], #get_autoincrement_key); }
;

set_autocommit:
    "set"! "autocommit"! "true"!
    { #set_autocommit = #([SET_AUTOCOMMIT_TRUE, "set_autocommit_true"], #set_autocommit); }
    |
    "set"! "autocommit"! "false"!
    { #set_autocommit = #([SET_AUTOCOMMIT_FALSE, "set_autocommit_false"], #set_autocommit); }
;

insert:
    "insert"! "into"! table_name
    ( column_list )?
    insert_values
    { #insert = #([INSERT, "insert"], #insert); }
;

insert_values:
	select
	| "values"^ OPEN_PAREN! insert_value (COMMA! insert_value)* CLOSE_PAREN!
;

column_list:
    OPEN_PAREN! column_name (COMMA! column_name)* CLOSE_PAREN!
    { #column_list = #([COLUMN_LIST, "column_list"], #column_list); }
;

create_table:
    "create"! "table"! ("if" "not"! "exists"!)? table_name
    ( "option" OPEN_PAREN! QUOTED_STRING CLOSE_PAREN! ) ?
    OPEN_PAREN column_def (COMMA! column_def)* CLOSE_PAREN!
    { #create_table = #([CREATE_TABLE, "create_table"], #create_table); }
;

create_index:
    "create"! ("unique")? "index"! identifier "on"! table_name
    column_list
    { #create_index = #([CREATE_INDEX, "create_index"], #create_index); }
;

drop_index:
    "drop"! "index"! identifier "on"! table_name
    { #drop_index = #([DROP_INDEX, "drop_index"], #drop_index); }
;

alter_table:
	"alter"! "table"! 
	(
	alter_table_drop_constraint
	| alter_table_rename
	| alter_table_add_column
	)
;

alter_table_add_column:
	table_name "add"! "column"! identifier datatype (("not")? "null")? ( "default" default_value ) ?
	{ #alter_table_add_column = #([ALTER_TABLE_ADD_COLUMN, "alter_table_add_column"], #alter_table_add_column); }
;

alter_table_drop_constraint:
	table_name "drop"! "constraint"! identifier
    { #alter_table_drop_constraint = #([ALTER_TABLE_DROP_CONSTRAINT, "alter_table_drop_constraint"], #alter_table_drop_constraint); }
;

alter_table_rename:
	table_name "rename"! "to"! identifier
    { #alter_table_rename = #([ALTER_TABLE_RENAME, "alter_table_rename"], #alter_table_rename); }
;

drop_table:
    "drop"! "table"! ("if" "exists"!)? table_name
    { #drop_table = #([DROP_TABLE, "drop_table"], #drop_table); }
;

column_def:
    identifier datatype ("autoincrement")? (("not")? "null")? ("primary" "key")? ( "default" default_value ) ?
    { #column_def = #([COLUMN_DEF, "column_def"], #column_def); }
    
    | "primary"! "key"! OPEN_PAREN! column_name (COMMA! column_name)* CLOSE_PAREN!
    { #column_def = #([PRIMARY_KEY, "primary_key"], #column_def); }
    
    | ("constraint"! identifier!)? "foreign"! "key"! 
      column_list "references"! table_name column_list
      /*
      ("on"! 
            ("delete" ("restrict" | "cascade" | "set"! "null" | "no" "action"! )) |
            ("update" ("restrict" | "cascade" | "set"! "null" | "no" "action"! ))
        )*
        */
    { #column_def = #([FOREIGN_KEY, "foreign_key"], #column_def); }
;
    
datatype:
    (
    type_int ( OPEN_PAREN! NUMBER! CLOSE_PAREN! )?
    | type_varchar OPEN_PAREN! NUMBER CLOSE_PAREN!
    | type_decimal ( OPEN_PAREN! NUMBER ( COMMA! NUMBER ) ? CLOSE_PAREN! ) ?
    | type_datetime
    | type_blob  ( OPEN_PAREN! NUMBER! CLOSE_PAREN! ) ?
    | type_clob  ( OPEN_PAREN! NUMBER! CLOSE_PAREN! ) ?
    )
    { #datatype = #([DATATYPE, "datatype"], #datatype); }
;

type_int:
    (
    "int"
    | "integer"
    | "tinyint"
    | "smallint"
    | "mediumint"
    | "bit"
    | "boolean"
    )
    { #type_int = #([TYPE_INT, "type_int"], #type_int); }
;

type_varchar:
    (
    "varchar" 
    | "char"
    )
    { #type_varchar = #([TYPE_VARCHAR, "type_varchar"], #type_varchar); }
;
    
type_decimal:
    (
    "decimal" 
    | "numeric"
    | "dec"
    | "real"
    | "float"
    | "double"
    | "bigint"
    )
    { #type_decimal = #([TYPE_DECIMAL, "type_decimal"], #type_decimal); }
;
    
type_datetime:
    (
    "datetime"
    | "date"
    | "timestamp"
    | "time"
    )
    { #type_datetime = #([TYPE_DATETIME, "type_datetime"], #type_datetime); }
;

type_blob:
    (
    "blob"
    | "tinyblob"
    | "mediumblob"
    | "longblob"
    | "binary"
    | "varbinary"
    | "longvarbinary"
    | "image"
    )
    { #type_blob = #([TYPE_BLOB, "type_blob"], #type_blob); }
;
    
type_clob:
    (
    "clob"
    | "tinytext"
    | "mediumtext"
    | "longtext"
    | "text"
    | "longvarchar"
    )
    { #type_clob = #([TYPE_CLOB, "type_clob"], #type_clob); }
;   

select:
    "select"! ("distinct")? ("top" NUMBER)? ( ASTERISK | select_list )
    "from"! table_list
    ( "where"! condition )?
    ( "group"! "by"! group_by )?
    ( "having" condition )?
    ( "order"! "by"! order_by )?
    ( "limit"! limit )?
    { #select = #([SELECT, "select"], #select); }
;

update:
    "update"! table_name
    "set"! update_set   
    ( COMMA! update_set )*
    ( "where"! condition ) ? 
    { #update = #([UPDATE, "update"], #update); }
;

update_set:
    column_name EQUAL! expression
    { #update_set = #([UPDATE_SET, "update_set"], #update_set); }
;

delete:
    "delete"! "from"! table_name 
    ( "where"! condition )?
    { #delete = #([DELETE, "delete"], #delete); }
;

select_list:
    (select_item ( COMMA! select_item )* )
;

table_list:
	(from_item ( COMMA! from_item)* )
;

from_item:
	selected_table | join
;

join:
    selected_table ("inner" | "left" "outer"!) "join"! selected_table "on"! condition
//    | selected_table "cross" "join"! selected_table
    { #join = #([JOIN, "join"], #join); }
;

select_item:
    (
      options { generateAmbigWarnings = false; }:
	( 
	  table_name DOT! ASTERISK!
		{ #select_item = #([TABLE_ASTERISK, "table_asterisk"], #select_item); }
	)
	|
    ( 
      expression ( alias )?
	    { #select_item = #([SELECT_ITEM, "select_item"], #select_item); }
    )
    )
;

table_name: 
    identifier 
;

alias: 
    ( "as"! )? identifier
;

aggregate:
    "count"^ OPEN_PAREN! ("distinct")? ( ASTERISK | column | function | value_no_null ) CLOSE_PAREN! 
    | "min"^ OPEN_PAREN! (column | function) CLOSE_PAREN!
    | "max"^ OPEN_PAREN! (column | function) CLOSE_PAREN!
    | "sum"^ OPEN_PAREN! (column | function) CLOSE_PAREN!
    | "avg"^ OPEN_PAREN! (column | function) CLOSE_PAREN!
;    

simple_function:
    "now"^  OPEN_PAREN! CLOSE_PAREN!
    | "current_date"^  OPEN_PAREN! CLOSE_PAREN!
    | "current_time"^  OPEN_PAREN! CLOSE_PAREN!
    | "current_timestamp"^  OPEN_PAREN! CLOSE_PAREN!
    | "curdate"^  OPEN_PAREN! CLOSE_PAREN!
    | "curtime"^  OPEN_PAREN! CLOSE_PAREN!
;

function:
	simple_function
    | "cast"^ OPEN_PAREN! expression "as"! datatype CLOSE_PAREN!
    | "convert"^ OPEN_PAREN! expression COMMA! datatype CLOSE_PAREN!

    | "length"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "char_length"^ OPEN_PAREN! expression CLOSE_PAREN!
    
    | "mod"^ OPEN_PAREN! expression COMMA! expression CLOSE_PAREN!
    
    | "concat"^ OPEN_PAREN! expression COMMA! expression CLOSE_PAREN!

    | "lower"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "lcase"^ OPEN_PAREN! expression CLOSE_PAREN!

    | "upper"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "ucase"^ OPEN_PAREN! expression CLOSE_PAREN!

    /*
    | "ifnull"^ OPEN_PAREN! expression COMMA! expression CLOSE_PAREN!
    | "sign"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "abs"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "ascii"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "char"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "substring"^ OPEN_PAREN! expression COMMA! expression (COMMA! expression)? CLOSE_PAREN! 
    | "left"^ OPEN_PAREN! expression COMMA! expression CLOSE_PAREN!
    | "right"^ OPEN_PAREN! expression COMMA! expression CLOSE_PAREN!
    | "year"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "month"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "dayofmonth"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "dayofweek"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "dayofyear"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "hour"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "minute"^ OPEN_PAREN! expression CLOSE_PAREN!
    | "second"^ OPEN_PAREN! expression CLOSE_PAREN!
    */
;

value_or_column:
    value
    | column
;

default_value:
    (
    "null"
        { #default_value = #([NULL_INSERT, "null_insert"], #default_value); }
    )
    | value_no_null 
    | (MINUS! decimal_value
        { #default_value = #([NEGATIVE, "negative"], #default_value); }
      )
;

insert_value:
    (
    "null"
        { #insert_value = #([NULL_INSERT, "null_insert"], #insert_value); }
    )
    | value_no_null 
    | (MINUS! decimal_value
        { #insert_value = #([NEGATIVE, "negative"], #insert_value); }
      )
    | simple_function
;

value_no_null:
    decimal_value
    | QUOTED_STRING 
    | PARAMETER
    | date
    | timestamp
    | binary
;

value:
    value_no_null
    | "null"
;

date:
    "date"! QUOTED_STRING
    { #date = #([DATE, "date"], #date); }
;

timestamp:
    "timestamp"! QUOTED_STRING
    { #timestamp = #([TIMESTAMP, "timestamp"], #timestamp); }
;

binary:
    HEX
    { #binary = #([BINARY, "binary"], #binary); }
;

decimal_value:
    (NUMBER
    | DOT NUMBER
    | NUMBER DOT (NUMBER)?
    )
    { #decimal_value = #([DECIMAL_VALUE, "decimal_value"], #decimal_value); }
;

column:
    ( table_name DOT! )? column_name
    { #column = #([COLUMN, "column"], #column); }
;

column_name: 
    identifier 
;

selected_table:
    ( table_name ) ( identifier )?
    { #selected_table = #([SELECTED_TABLE, "selected_table"], #selected_table); }
;

group_by:
    column ( COMMA! column )*
    { #group_by = #([GROUP_BY, "group_by"], #group_by); }
;

order_by:
    order_item ( COMMA! order_item )*
    { #order_by = #([ORDER_BY, "order_by"], #order_by); }
;

order_item:
    ( column ) ( "asc" | "desc" )? 
    { #order_item = #([ORDER_ITEM, "order_item"], #order_item); }
;

limit:
    NUMBER ( "offset"! NUMBER )?
    { #limit = #([LIMIT, "limit"], #limit); }
;

//
// Direct mappings to lexer.
//

identifier:
    IDENTIFIER
;

quoted_string: 
    QUOTED_STRING 
;

//
// SQL-92 keywords
//

keyword:
    "abs" | "add" | "all" | "alter" | "and" | "as" |
    "asc" | "avg" | "before" | "between" | "bigint" |
    "binary" | "bit" | "blob" | "boolean" | "both" |
    "by" | "cached" | "cascade" | "case" | "cast" |
    "char" | "character" | "character_length" |
    "char_length" | "clob" | "column" | "commit" |
    "concat" | "constraint" | "count" | "create" |
    "cross" | "current_date" | "current_time" |
    "current_timestamp" | "database" | "date" |
    "datetime" | "dec" | "decimal" | "default" |
    "delete" | "desc" | "distinct" | "double" |
    "drop" | "exists" | "extract" | "false" | "float" |
    "for" | "foreign" | "from" | "grant" | "group" |
    "having" | "if" | "image" | "in" | "index" |
    "infile" | "inner" | "insert" | "int" | "integer" |
    "into" | "is" | "join" | "key" | "kill" |
    "leading" | "left" | "length" | "like" | "limit" |
    "lineno" | "load" | "lob" | "local" | "locate" |
    "lock" | "long" | "longvarbinary" | "longvarchar" |
    "lower" | "match" | "max" | "mediumint" | "min" |
    "mod" | "natural" | "not" | "null" | "numeric" |
    "object" | "octet_length" | "on" | "option" |
    "or" | "order" | "other" | "outer" | "outfile" |
    "position" | "precision" | "primary" |
    "privileges" | "procedure" | "read" | "real" |
    "references" | "rename" | "replace" | "restrict" |
    "returns" | "revoke" | "right" | "rollback" |
    "savepoint" | "select" | "session_user" | "set" |
    "smallint" | "sqrt" | "substring" | "sum" |
    "sysdate" | "table" | "temp" | "text" | "time" |
    "timestamp" | "tinyint" | "to" | "top" | "trailing" |
    "trigger" | "trim" | "true" | "union" | "unique" |
    "unsigned" | "update" | "upper" | "user" |
    "using" | "values" | "varbinary" | "varchar" |
    "varchar_ignorecase" | "when" | "where" | "with" |
    "write" | "zerofill"
    /*
    "abs" | "access" | "action" | "add" | "admin" |
    "after" | "all" | "alter" | "and" | "any" | "as" |
    "asc" | "ascending" | "at" | "atomic" | "audit" |
    "authorization" | "avg" | "backup" | "before" |
    "begin" | "between" | "bigint" | "binary" | "bit" |
    "blob" | "boolean" | "both" | "break" | "browse" |
    "bulk" | "by" | "call" | "cascade" | "cascaded" |
    "case" | "cast" | "char" | "character" |
    "character_length" | "char_length" | "check" |
    "checkpoint" | "class" | "clob" | "cluster" |
    "clustered" | "column" | "comment" | "commit" |
    "committed" | "compress" | "compute" | "concat" |
    "connect" | "constraint" | "contains" | "count" |
    "country" | "create" | "cross" | "current" |
    "current_database" | "current_date" |
    "current_lsn" | "current_path" | "current_schema" |
    "current_session" | "current_time" |
    "current_timestamp" | "current_user" | "data" |
    "database" | "datalog" | "date" | "datetime" |
    "day" | "dbcc" | "dec" | "decimal" | "default" |
    "deferrable" | "delete" | "desc" | "descending" |
    "deterministic" | "diagnostics" | "disconnect" |
    "disk" | "distinct" | "double" | "drop" | "dump" |
    "each" | "encrypted" | "end" | "errlvl" |
    "escape" | "except" | "exclusive" | "execute" |
    "exists" | "exit" | "external" | "extract" |
    "false" | "file" | "fillfactor" | "filter_column" |
    "filter_row" | "float" | "for" | "force" |
    "foreign" | "from" | "full" | "function" |
    "getlastlsn" | "global" | "grant" | "granted" |
    "group" | "having" | "heap" | "holdlock" | "hour" |
    "identified" | "identity" | "identity_insert" |
    "if" | "image" | "immediate" | "in" | "increment" |
    "index" | "indexonly" | "infile" | "initial" |
    "initially" | "inner" | "inout" | "insert" |
    "int" | "integer" | "intersect" | "into" | "is" |
    "isolation" | "java" | "join" | "key" | "kill" |
    "language" | "large" | "leading" | "left" |
    "length" | "level" | "like" | "limit" | "lineno" |
    "load" | "lob" | "local" | "locate" | "lock" |
    "logmarker" | "long" | "longraw" |
    "longvarbinary" | "longvarchar" | "lower" |
    "lsn_current_id" | "lsn_current_offset" |
    "lsn_skip_id" | "lsn_skip_offset" |
    "lsn_start_id" | "lsn_start_offset" | "match" |
    "max" | "maxextents" | "mediumint" | "method" |
    "min" | "minus" | "minute" | "mod" | "mode" |
    "modifies" | "month" | "natural" | "new" |
    "newsnapshot" | "newupdate" | "no" | "noaudit" |
    "nocompress" | "nonclustered" | "non_reentrant" |
    "not" | "nowait" | "null" | "number" | "numeric" |
    "object" | "octet_length" | "of" | "off" |
    "offline" | "offsets" | "old" | "on" | "online" |
    "only" | "option" | "or" | "order" |
    "organization" | "other" | "out" | "outer" |
    "outfile" | "over" | "pagesize" | "param" |
    "parameter" | "password" | "path" | "pctfree" |
    "plan" | "planonly" | "position" | "precision" |
    "preserve" | "primary" | "print" | "prior" |
    "privileges" | "proc" | "procedure" |
    "publication" | "raiserror" | "raw" | "read" |
    "reads" | "readtext" | "real" | "reconfigure" |
    "reentrant" | "references" | "referencing" |
    "release" | "rename" | "repeatable" | "replace" |
    "restrict" | "return" | "returns" | "revoke" |
    "right" | "role" | "rollback" | "routine" | "row" |
    "rowcount" | "rows" | "rule" | "save" |
    "savepoint" | "scalar" | "schema" | "second" |
    "select" | "serializable" | "session_user" |
    "set" | "setearliestlsn" | "setuser" | "shutdown" |
    "signal" | "size" | "smallint" | "snapshot" |
    "some" | "specific" | "sql" | "sqlstate" | "sqrt" |
    "start" | "statement" | "statistics" | "style" |
    "subscription" | "substring" | "sum" | "sysdate" |
    "systime" | "systimestamp" | "table" | "temp" |
    "temporary" | "text" | "textsize" | "time" |
    "timestamp" | "tinyint" | "to" | "trailing" |
    "tran" | "transaction" | "transaction_name" |
    "trigger" | "trim" | "true" | "truncate" |
    "tsequal" | "uncommitted" | "union" | "unique" |
    "unisync" | "unknown" | "unsigned" | "update" |
    "upper" | "user" | "using" | "values" |
    "varbinary" | "varchar" | "varchar2" |
    "varchar_ignorecase" | "view" | "wait" |
    "waitfor" | "when" | "where" | "while" | "with" |
    "work" | "write" | "writetext" | "year" |
    "zerofill"
*/
    ;

//
// Lexer
//

class SQLLexer extends Lexer;

options {
    exportVocab = SQL;
    k = 4;
    caseSensitive = false;
    caseSensitiveLiterals = false;
    // for some reason, Antlr fills half of the bitset with zeroes
    // so trick Antlr with a double size bitset
    // correct would be:
    // charVocabulary = '\3' .. '\377';
    charVocabulary='\u0003'..'\u01FF';
}

//
// Literals were put here in the lexer to get them to be case insensitive.
//

IDENTIFIER: 
    'a' .. 'z' ( 'a' .. 'z' | '0' .. '9' | '_' )*
;

QUOTED_STRING: 
    '\'' ( ~'\'' )* '\'' 
    // a construct like 'hello''world' could be one string or two
    // actually it is one string with a ' in the middle
    ( //options { warnWhenFollowAmbig = false; }:
        options {greedy=true;}:
      QUOTED_STRING )*
;

DOT: '.' ;
COMMA: ',' ;
ASTERISK: '*' ;
OPEN_PAREN: '(' ;
CLOSE_PAREN: ')' ;
PLUS: '+' ;
MINUS: '-' ;
DIVIDE: '/' ;
VERTBARS: "||" ;
PARAMETER: '?' ;
EQUAL: '=' ;
BIGGER: '>' ;
SMALLER: '<' ;
NOT_EQUAL: "<>" ;
NOT_EQUAL_2: "!=" ;
BIGGER_EQUAL: ">=";
SMALLER_EQUAL: "<=";

HEX:
    'x' '\'' ( ~'\'' )* '\'' 
;


NUMBER:
    '0'..'9'('0'..'9')*
;

WS:
    (   ' '
    |   '\t'
    |   '\n'      
    |   '\r'
    )
    {$setType(Token.SKIP);} //ignore this token
;


START_END_REMARK:
    "/*" 
    ( { LA(2)!='/' }? '*' | ~'*' )*
    "*/"
    {$setType(Token.SKIP);}
;
	
SINGLE_LINE_COMMENT:
    "--"
    ( ~( '\n' | '\r' ) )* 
    ( '\n' | '\r' ( '\n' )? )
    {$setType(Token.SKIP);}
;
