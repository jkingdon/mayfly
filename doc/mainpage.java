/**
@mainpage

@section motivation Motivation

<p>Although there are a vast number of free and non-free SQL
implementations in Java, few of them are really good for unit testing.  A
database for unit testing should be very fast on small data sets and
should not present performance or other obstacles to setting up
databases, adding and removing tables, and destroying databases.  A
typical unit test run will want to create thousands of databases.
With most SQL databases, this is not achievable, and one must resort
to compromises such as keeping the tables around and only setting up
data on each unit test run.  Mayfly aims to make creating an in-memory
SQL database as easy as creating any other in-memory data structure.</p>

<p>One key feature is the ability to very cheaply snapshot the database
(both metadata and data) and restore from that point.  This is further
described at {@link net.sourceforge.mayfly.Database#dataStore()}.</p>

@section installation Installation

Download Mayfly and put mayfly.jar in your classpath.  You are
now ready to write your unit tests to connect to mayfly.

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

@section syntax SQL Syntax

We don't want you to have to rewrite your SQL for Mayfly versus your
production database.  Of course, this ideal is not 100% realized due
to the wide variation in SQL dialects, and the fact that we simply
haven't gotten around to adding everything to Mayfly which we'd like
to.  Your odds are generally better if you stay close to standard
SQL.

This section describes the syntax supported in Mayfly.  In this description,
Square brackets indicate optional
elements, vertical bars indicate choices, curly braces simply group
elements, and <tt>, ...</tt> indicates
that the previous element can be repeated.
Parentheses stand for themselves.

You may want to read these descriptions with reference to a more comprehensive
SQL guide, such as those found at <a href="http://www.sqlzoo.net/" >sqlzoo.net</a>.
Here we emphasis conciseness and identifying our SQL subset, instead of a full
description of the semantics and syntax.

@subsection create CREATE TABLE

<pre>
CREATE TABLE <var>name</var> ({<var>column-name</var> <var>data-type</var> }, ...)
</pre>

Supported data types are INTEGER and VARCHAR(<var>size</var>).

@subsection drop DROP TABLE

<pre>
DROP TABLE <var>name</var>
</pre>

@subsection insert INSERT

<pre>
INSERT INTO <var>table</var> [ ( <var>column-name</var>, ... ) ]
  VALUES ( <var>expression</var>, ... )
</pre>

@subsection select SELECT

<pre>
SELECT { * | <var>alias</var>.* | [<var>alias</var> . ] <var>column</var>}, ...
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
  <var>expression</var> IS [ NOT ] NULL |
  <var>expression</var> IN ( <var>expression</var>, ... )
</pre>
  
An <var>expression</var> is:

<pre>
  <var>0-9...</var> |
  <var>'<var>character</var>...'</var> |
  [ <var>alias</var> . ] <var>column</var> |
  NULL
</pre>

*/
