package net.sourceforge.mayfly.evaluation.expression;

import static net.sourceforge.mayfly.util.MayflyAssert.assertLocation;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

public class FunctionTest extends TestCase {
    
    public void testSeparateClassAndMethod() throws Exception {
        Function function = makeFunction("java.lang.Math.abs");
        assertEquals("java.lang.Math", function.className);
        assertEquals("abs", function.methodName);
    }

    public void testNoPeriod() throws Exception {
        try {
            new Function("abs", 
                new ImmutableList(),
                new Location(5, 6, 7, 8),
                new Options());
            fail();
        }
        catch (MayflyException e) {
            assertEquals("function name abs does not contain a period",
                e.getMessage());
            assertLocation(5, 6, 7, 8, e.location());
        }
    }
    
    public void testNoSuchClass() throws Exception {
        try {
            new Function("net.sourceforge.mayfly.evaluation.NoSuchClass.x",
                new ImmutableList(),
                new Location(5, 6, 7, 8),
                new Options());
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "function name specifies Java class " +
                "net.sourceforge.mayfly.evaluation.NoSuchClass " +
                "which is not found",
                e.getMessage());
            assertLocation(5, 6, 7, 8, e.location());
        }
    }

    public void testClassNameToClass() throws Exception {
        Function function = makeFunction("java.lang.Math.abs");
        assertEquals("java.lang.Math", function.classObject.getName());
    }
    
    public void not_yet_working_testNoSuchMethod() throws Exception {
        try {
            new Function(getClass().getName() + ".noSuch",
                new ImmutableList(),
                new Location(5, 6, 7, 8),
                new Options());
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "function name specifies method noSuch which is not found in " +
                getClass().getName(),
                e.getMessage());
            assertLocation(5, 6, 7, 8, e.location());
        }
    }

    private Function makeFunction(String name) {
        Function function = new Function(name, 
            new ImmutableList(),
            Location.UNKNOWN,
            new Options());
        return function;
    }
    
}
