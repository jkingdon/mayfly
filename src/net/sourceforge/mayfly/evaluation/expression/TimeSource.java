package net.sourceforge.mayfly.evaluation.expression;

/**
Someday we might need to worry about getting the "current" time
from a transaction log or synchronized across replicated databases
or something.  That day is not yet here.

But we do already have a need to inject a time source for testing.
*/

public abstract class TimeSource {

    abstract public long current();

}
