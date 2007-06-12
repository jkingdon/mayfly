/**
@mainpage

@section introduction Introduction

<p>Mayfly is an SQL implementation in Java which is intended
for unit testing.  You'll be linking the Mayfly jars into your
application, and then writing unit tests which create mayfly
databases (as in-memory objects) and connect to them (using
the JDBC database interface or slight variations thereof).</p>

<p>One key feature is the ability to very cheaply snapshot the database
(both metadata and data) and restore from that point.  This is further
described at {@link net.sourceforge.mayfly.Database#dataStore()}.</p>

@section installation Installation

Download mayfly-<var>version</var>.zip and put the supplied jar files in your classpath.  You 
probably want to first write a simple unit test which just creates
a mayfly database, connects to it, and executes a few SQL commands.
It's easier to make that work first and subsequently hook in Mayfly to the
rest of your application.

The file mayfly-<var>version</var>-src-ide.zip within the main
download is designed to make it easy to browse Mayfly source (for example,
using the "attach sources"
feature found in Java development tools).  Either the sources or the
Mayfly documentation can help you distinguish between
methods you would ordinarily call and methods which are public but would
generally only be called from within Mayfly.

The source download mayfly-<var>version</var>-src.zip contains everything
you need to rebuild Mayfly and run the Mayfly tests.  Because it contains
jar files for non-Mayfly databases, it is rather large.

@section connecting Connecting

The easiest way to create a database and connect to it is to call
{@link net.sourceforge.mayfly.Database#Database()} to create a database object
and then call {@link net.sourceforge.mayfly.Database#openConnection()} to open a
JDBC connection to it.

Alternately, Mayfly supplies a JDBC Driver, described in
{@link net.sourceforge.mayfly.JdbcDriver}.

The most commonly used parts of JDBC are implemented, including PreparedStatement
and parameters.  As usual, if you get an
{@link net.sourceforge.mayfly.UnimplementedException}, we'd love to hear
what features you'd like to see us add (via the mayfly mailing list).

@section errors Errors

Because Mayfly is intended to be used during development, 
it is a Mayfly goal to detect errors as soon as possible, and give
an informative message.  For example, if you specify <tt>WHERE a = NULL</tt>
(instead of <tt>WHERE a IS NULL</tt>), Mayfly complains.  For another
example, if you specify a <tt>CREATE TABLE</tt> with duplicate column
names, Mayfly tells you which column was duplicated.  And so on.

If you find a problem with your SQL, and Mayfly gives an unclear or
less than helpful message, we consider that to be a bug.  Please
let us know, on the mayfly mailing list, what you were doing and what
would have helped you find your problem faster.

@section dump SQL Dumps

Mayfly has the ability to dump a database as an SQL script.  See
{@link net.sourceforge.mayfly.dump.SqlDumper}.

@section syntax SQL Syntax

We don't want you to have to rewrite your SQL for Mayfly versus your
production database.  Of course, this ideal is not 100% realized due
to the wide variation in SQL dialects, and the fact that we simply
haven't gotten around to adding everything to Mayfly which we'd like
to.  Your odds are generally better if you stay close to standard
SQL.  Mayfly does deliberately omit some syntaxes which seem to 
be little-used and/or non-portable, but in this case there is an
alternate syntax which will also work with non-Mayfly databases.

This section describes the syntax supported in Mayfly.  In this description,
Square brackets "[]" indicate optional
elements, vertical bars "|" indicate choices, curly braces "{}" simply group
elements, "..." indicates that the previous element can be repeated,
and ", ..." indicates
that the previous element can be repeated, with commas separating each
repeat.  Parentheses "()" stand for themselves.

These descriptions should be read with reference to a more comprehensive
SQL guide, such as those found at <a href="http://www.sqlzoo.net/" >sqlzoo.net</a>.
Here we emphasis conciseness and identifying our SQL subset, instead of a full
description of the semantics and syntax.

@subsection comments

Text from -- to a newline, or between 
<tt>/*</tt> and <tt>*</tt><tt>/</tt>, is a comment. 

@subsection alter ALTER TABLE

<pre>
ALTER TABLE <var>table-name</var> DROP COLUMN <var>column-name</var>
ALTER TABLE <var>table-name</var> ADD COLUMN <var>column-definition</var>
ALTER TABLE <var>table-name</var> MODIFY COLUMN <var>column-definition</var>
ALTER TABLE <var>table-name</var> 
  CHANGE COLUMN <var>old-name</var> <var>column-definition</var>
ALTER TABLE <var>table-name</var> DROP FOREIGN KEY <var>constraint-name</var>
ALTER TABLE <var>table-name</var> ADD <var>constraint</var>
</pre>

where <var>column-definition</var> is as in CREATE TABLE (a column
name and data type, roughly), and <var>constraint</var> is as in
CREATE TABLE (that is, starts with CONSTRAINT, UNIQUE, PRIMARY KEY,
or FOREIGN KEY).

@subsection createindex CREATE INDEX

<pre>
CREATE [UNIQUE] INDEX <var>index-name</var> 
  ON <var>table</var>(<var>column</var> [(<var>width</var>)], ...)
</pre>

This is a no-op unless the word UNIQUE is specified in which case it
is the same as defining a unique constraint (for example, in a
CREATE TABLE statement).  The latter syntax is generally preferred,
as it separates constraints (defining correct behavior) from indexes
(performance optimization).

@subsection create CREATE TABLE

<pre>
CREATE TABLE <var>table-name</var> (
  {
    {<var>column-name</var> <var>data-type</var> 
        [ DEFAULT <var>default-value</var> ]
        [ AUTO_INCREMENT |
          GENERATED BY DEFAULT AS IDENTITY
            [(STARTS WITH <var>value</var>)]
        ]
        {[ NOT NULL | UNIQUE | PRIMARY KEY ]}... 
    } |

    [ CONSTRAINT <var>name</var> ] UNIQUE(<var>column</var>, ...) |

    [ CONSTRAINT <var>name</var> ] PRIMARY KEY(<var>column</var>, ...) |

    [ CONSTRAINT <var>name</var> ]
      FOREIGN KEY(<var>column</var>) REFERENCES <var>table</var>(<var>column</var>)
      [ ON DELETE <var>action</var> [ ON UPDATE <var>action</var> ] |
        ON UPDATE <var>action</var> [ ON DELETE <var>action</var> ]
      ]
  }, ...
)
</pre>

For the most part, the data type is enforced fairly strictly.  Mayfly
does not automatically convert between one type and another as readily
as some SQL databases.

Supported data types for numbers are TINYINT, SMALLINT, INTEGER and BIGINT (8, 16, 32, and 64
bit integers, respectively).  DECIMAL(x,y) is supported.
For strings, there is VARCHAR(<var>size</var>) (with TEXT
as a non-standard synonym).  There is partial support for DATE.

Mayfly has two kinds of incrementing columns:
<ul>

<li>An identity column is enabled by GENERATED BY DEFAULT syntax.  
It is based on
a sequence (that is, the value to be inserted by default is not affected
by other insert statements into the table).</li>

<li>An auto-increment column is enabled by the AUTO_INCREMENT syntax.
It is based on the existing values (that is, inserting another value
into the table will change the value to be inserted).</li>
</ul>
Also, IDENTITY or SERIAL as datatypes indicate an incrementing column
(currently auto-increment rather than identity and INTEGER for the type, 
but subject to change to improve compatibility or general usefulness).

Foreign key actions for ON DELETE are NO ACTION, CASCADE, SET NULL, 
and SET DEFAULT.  Foreign key actions for ON UPDATE are NO ACTION only.

@subsection drop DROP TABLE

<pre>
DROP TABLE <var>name</var>
DROP TABLE <var>name</var> IF EXISTS
DROP TABLE IF EXISTS <var>name</var>
</pre>

Remove the table <var>name</var> and all its contents.
Without IF EXISTS, there must be a table by that name.
With the IF EXISTS (in either position), if there is no
table by that name, the command does nothing, without
an error.

@subsection insert INSERT

<pre>
INSERT INTO <var>table</var> [ ( <var>column-name</var>, ... ) ]
  VALUES ( <var>expression</var>, ... )
</pre>

If <var>expression</var> is DEFAULT, the default value that was
specified when creating the table is inserted.

<pre>
INSERT INTO <var>table</var>() VALUES ()
</pre>

Insert default values into every column (different databases have
different syntaxes for this).

<pre>
INSERT INTO <var>table</var> SET { <var>column-name</var> = <var>expression</var> }, ...
</pre>

This version is not standard SQL; it is an extension taken from MySQL.
So don't use it if you want portability.  But the potential added readibility
gained by putting the column name next to the corresponding expression may
justify it.

@subsection select SELECT

<pre>
SELECT { 
  * |
  <var>alias</var>.* |
  <var>expression</var> [ AS <var>column-alias</var> ], ...
FROM <var>table-reference</var>, ...
[ WHERE <var>condition</var> ]
[ ORDER BY { [<var>alias</var> .] <var>column</var> }, ... ]
[ LIMIT <var>count</var> [ OFFSET <var>start</var> ] ]
</pre>

A <var>table-reference</var> is:

<pre>
  <var>tablename</var> [ <var>alias</var> ] |
  <var>table-reference</var>
    { INNER | LEFT OUTER } JOIN <var>table-reference</var>]
    ON <var>condition</var>
</pre>

A <var>condition</var> is:

<pre>
  <var>condition</var> OR <var>condition</var> |
  <var>condition</var> AND <var>condition</var> |
  NOT <var>condition</var> |
  <var>expression</var> = <var>expression</var> |
  <var>expression</var> { != | <> } <var>expression</var> |
  <var>expression</var> < <var>expression</var> |
  <var>expression</var> > <var>expression</var> |
  <var>expression</var> <= <var>expression</var> |
  <var>expression</var> >= <var>expression</var> |
  <var>expression</var> IS [ NOT ] NULL |
  <var>expression</var> IN ( <var>expression</var>, ... )
  <var>expression</var> IN ( <var>subselect</var> )
  <var>expression</var> LIKE <var>pattern</var> |
</pre>
  
An <var>expression</var> is:

<pre>
  <var>0-9...</var> |
  <var>x'<var>hexdigit</var>...'</var> |
  <var>'<var>character</var>...'</var> |
  [ <var>alias</var> . ] <var>column</var> |
  <var>expression</var> + <var>expression</var> |
  <var>expression</var> - <var>expression</var> |
  <var>expression</var> * <var>expression</var> |
  <var>expression</var> / <var>expression</var> |
  <var>expression</var> || <var>expression</var> |
  ( <var>expression</var> ) |
  ( <var>subselect</var> ) |
  MAX ( [ ALL | DISTINCT ] <var>expression</var> ) |
  MIN ( [ ALL | DISTINCT ] <var>expression</var> ) |
  SUM ( [ ALL | DISTINCT ] <var>expression</var> ) |
  AVG ( [ ALL | DISTINCT ] <var>expression</var> ) |
  COUNT ( { [ ALL | DISTINCT ] <var>expression</var> } | * ) }
  NULL |
  CASE { WHEN <var>condition</var> THEN <var>expression</var> }...
    [ ELSE <var>else-expression</var> ]
    END
</pre>

A <var>subselect</var> is just a SELECT statement.

There is also some support for GROUP BY and HAVING.

@subsection select UPDATE

UPDATE <var>table</var> {SET <var>column</var> = <var>expression</var> }, ...
  [WHERE <var>condition</var>]

If <var>expression</var> is DEFAULT, the default value that was
specified when creating the table is used.

@section schemas Schemas

A Database object can contain several <i>schemas</i> - each one
has its own tables and they do not interact with each other.
Currently, you must call the SET SCHEMA command to select which
schema you are going to operate on.  Support for the syntax
schema.table or schema.table.column is planned for the future
but is not there now.

The syntax of the schema commands is:

<pre>
CREATE SCHEMA name [AUTHORIZATION DBA] 
  [ { <var>create-table-command</var> } ... ]

SET SCHEMA name
</pre>

@section transactions Transactions and Threads

It is not yet safe to share a database between several threads.

Furthermore, even those aspects of transactions which are
visible from within a single thread (for example, rollback),
and not yet implemented.

@section optimize Query Optimization

For the most part, queries are un-optimized.  Most unit tests will
only have a few rows in each table, and in that environment it will
generally be faster for Mayfly to look at every row (a table scan)
on read rather than build an index on writes.

One can declare an index but this is a no-op.  For example:
<pre>
    CREATE TABLE(a INTEGER, INDEX(a))
</pre>

Having said that, it is now possible to,
for example, implicitly join 3 tables of 1000 rows each, 
in certain specific cases.  The query optimizer is still dead simple -
it proceeds from left to right and can decompose a WHERE
clause made up of AND.  For example, 
"SELECT * from foo, bar, baz where foo.x = bar.x"
will get optimized to
<pre>
inner join foo and bar on foo.x = bar.x
inner join that with baz
</pre>
whereas
"SELECT * from foo, bar, baz where bar.x = baz.x"
will perform all the joins, and only
then apply the WHERE (requiring temporary storage
of the number of rows in foo times the number of rows
in bar times the number of rows in baz).  Explicit
joins are executed as they are written.  For example
"SELECT * from foo, bar INNER JOIN baz ON bar.x = baz.x"
will select based on the ON before joining with the foo table.

@section References

One good reference is <i>SQL in a Nutshell</i>, by Kline, Kline and Hunt,
published by O'Reilly.
It has a detailed guide to syntax and semantics, including the SQL2003 standard and
the differences among the most common SQL implementations.

A more introductory book is <i>The Practical SQL Handbook: Using Structured Query Language</i>,
by Judith S. Bowman, Sandra L. Emerson, and Marcy Darnovsky (third edition is from 1996).

One good SQL reference/tutorial web site is <a href="http://www.sqlzoo.net/" >SQLzoo</a>.

*/
