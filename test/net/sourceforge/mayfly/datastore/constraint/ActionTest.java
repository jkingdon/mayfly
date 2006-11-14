package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

public class ActionTest extends TestCase {
    
    public void testDump() throws Exception {
        assertEquals("SET NULL", dump(new SetNull()));
        assertEquals("NO ACTION", dump(new NoAction()));
        assertEquals("SET DEFAULT", dump(new SetDefault()));
        assertEquals("CASCADE", dump(new Cascade()));
    }

    private String dump(Action action) throws IOException {
        StringWriter out = new StringWriter();
        action.dump(out);
        return out.toString();
    }

}
