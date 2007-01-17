package net.sourceforge.mayfly.evaluation.expression;

/**
 @internal
Someday we might need to worry about getting the "current" time
from a transaction log or synchronized across replicated databases
or something.  That day is not yet here.

But we do already have a need to inject a time source for testing.
Right now that is just used by Mayfly's own tests.  But we should
figure out a way to export it to whoever is calling Mayfly.
They will surely appreciate it if they can inject their own
value for CURRENT_TIMESTAMP.
*/

public abstract class TimeSource {

    abstract public long current();

}
