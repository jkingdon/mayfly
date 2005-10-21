package net.sourceforge.mayfly.ldbc;

import junit.framework.TestCase;
import net.sourceforge.mayfly.util.*;

public class RowsTest extends TestCase {
    public void testJoin() throws Exception {
        Rows a =
            new Rows(
                new L()
                    .append(
                        new M()
                            .entry("colA", "1a")
                            .entry("colB", "1b")
                            .asImmutable()
                    )
                    .asImmutable()
            );

        Rows b =
            new Rows(
                new L()
                    .append(
                        new M()
                            .entry("colX", "1x")
                            .entry("colY", "1y")
                            .asImmutable()
                    )
                    .append(
                        new M()
                            .entry("colX", "2x")
                            .entry("colY", "2y")
                            .asImmutable()
                    )
                    .asImmutable()
            );

        assertEquals(
            new Rows(
                new L()
                    .append(
                        new M()
                            .entry("colA", "1a")
                            .entry("colB", "1b")
                            .entry("colX", "1x")
                            .entry("colY", "1y")
                            .asImmutable()
                    )
                    .append(
                        new M()
                            .entry("colA", "1a")
                            .entry("colB", "1b")
                            .entry("colX", "2x")
                            .entry("colY", "2y")
                            .asImmutable()
                    )
                    .asImmutable()
            ),
            a.join(b)
        );


    }
}