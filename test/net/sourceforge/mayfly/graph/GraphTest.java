package net.sourceforge.mayfly.graph;

import junit.framework.TestCase;
import junitx.framework.StringAssert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;

public class GraphTest extends TestCase {
    
    private Graph graph;
    private StringNode a;
    private StringNode b;
    private StringNode c;
    private StringNode d;
    private StringNode e;

    public GraphTest() {
        graph = new Graph();
        a = new StringNode("a");
        b = new StringNode("b");
        c = new StringNode("c");
        d = new StringNode("d");
        e = new StringNode("e");
    }

    public void testEmpty() throws Exception {
        assertEquals(0, graph.nodeCount());
    }
    
    public void testAddNode() throws Exception {
        graph.addNode(new StringNode("a"));
        assertEquals(1, graph.nodeCount());
        graph.addNode(new StringNode("b"));
        assertEquals(2, graph.nodeCount());
    }
    
    public void testDuplicateNode() throws Exception {
        graph.addNode(a);
        
        try {
            graph.addNode(a);
            fail();
        }
        catch (RuntimeException e) {
            StringAssert.assertStartsWith("already have node ", e.getMessage());
        }
    }
    
    public void testBackupCompareIsZero() throws Exception {
        graph.addNode(new StringNode("a"));
        
        try {
            graph.addNode(new StringNode("a"));
            fail();
        }
        catch (RuntimeException e) {
            StringAssert.assertStartsWith("already have node ", e.getMessage());
        }
    }
    
    public void testAddEdge() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addEdge(a, b);
        
        assertEquals(0, graph.predecessors(a).size());

        SortedSet bPredecessors = graph.predecessors(b);
        assertEquals(1, bPredecessors.size());
        assertSame(a, bPredecessors.iterator().next());
        
        assertEquals(0, graph.successors(b).size());

        SortedSet aSuccessors = graph.successors(a);
        assertEquals(1, aSuccessors.size());
        assertSame(b, aSuccessors.iterator().next());
    }

    public void testAddEdgeAgainIsNoop() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addEdge(a, b);
        graph.addEdge(a, b);
        
        assertEquals(0, graph.predecessors(a).size());

        SortedSet bPredecessors = graph.predecessors(b);
        assertEquals(1, bPredecessors.size());
        assertSame(a, bPredecessors.iterator().next());
        
        assertEquals(0, graph.successors(b).size());

        SortedSet aSuccessors = graph.successors(a);
        assertEquals(1, aSuccessors.size());
        assertSame(b, aSuccessors.iterator().next());
    }

    public void testRemoveEdge() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addEdge(a, b);

        graph.removeEdge(a, b);

        assertEquals(0, graph.predecessors(a).size());
        assertEquals(0, graph.predecessors(b).size());
        assertEquals(0, graph.successors(a).size());
        assertEquals(0, graph.successors(b).size());
    }

    public void testRemoveNonexistentEdge() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        try {
            graph.removeEdge(a, b);
            fail();
        }
        catch (RuntimeException e) {
            StringAssert.assertStartsWith(
                "there was no edge from ", e.getMessage());
        }
    }
    
    public void testNodesWithNoPredecessor() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addEdge(a, b);
        
        SortedSet initialNodes = graph.nodesWithNoPredecessor();
        assertEquals(1, initialNodes.size());
        assertSame(a, initialNodes.iterator().next());
    }

    public void testSortingOfInitialNodes() throws Exception {
        graph.addNode(a);
        graph.addNode(e);
        graph.addNode(d);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(a, b);
        
        Iterator initialNodes = graph.nodesWithNoPredecessor().iterator();
        assertSame(a, initialNodes.next());
        assertSame(c, initialNodes.next());
        assertSame(d, initialNodes.next());
        assertSame(e, initialNodes.next());
        assertFalse(initialNodes.hasNext());
    }
    
    public void testTopologicalSort() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addEdge(d, b);
        graph.addEdge(c, b);
        graph.addEdge(b, a);

        Iterator sorted = graph.topologicalSort().iterator();
        assertSame(c, sorted.next());
        assertSame(d, sorted.next());
        assertSame(b, sorted.next());
        assertSame(a, sorted.next());
        assertFalse(sorted.hasNext());
    }
    
    public void testTopologicalSortWithCycle() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addEdge(d, b);
        graph.addEdge(c, b);
        graph.addEdge(b, a);
        graph.addEdge(a, d);

        try {
            graph.topologicalSort();
            fail();
        }
        catch (CycleDetectedException e) {
            /* Ideally, this would (a) list all the members of one cycle,
               or perhaps an abbreviated version if there are many,
               and (b) be re-worded for the specific graph (perhaps by
               having the CycleDetectedException contain references to
               the nodes in the cycle, 
               and letting the caller catch and rethrow). */
            assertEquals("graph contains a cycle", e.getMessage());
        }
    }
    
    public void testRanOutOfNodes() throws Exception {
        try {
            graph.sortStep(new ArrayList(), Collections.EMPTY_LIST);
            fail();
        }
        catch (CycleDetectedException e) {
            assertEquals("graph contains a cycle", e.getMessage());
        }
    }
    
    public void testNoSuccessors() throws Exception {
        graph.addNode(a);

        ArrayList queue = new ArrayList();
        queue.add(a);
        ArrayList result = new ArrayList();
        graph.sortStep(result, queue);
        
        assertEquals(1, result.size());
        assertSame(a, result.get(0));
        
        assertEquals(0, queue.size());
    }

    public void testSuccessorHasOtherPredecessors() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(a, c);
        graph.addEdge(b, c);

        ArrayList queue = new ArrayList();
        queue.add(a);
        queue.add(b);
        ArrayList result = new ArrayList();
        graph.sortStep(result, queue);
        
        assertEquals(1, result.size());
        assertSame(a, result.get(0));
        
        assertEquals(1, queue.size());
        assertSame(b, queue.get(0));
        
        Iterator cPredecessors = graph.predecessors(c).iterator();
        assertSame(b, cPredecessors.next());
        assertFalse(cPredecessors.hasNext());
    }

    public void testMoveSuccessorsToQueue() throws Exception {
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(a, b);
        graph.addEdge(a, c);

        ArrayList queue = new ArrayList();
        queue.add(a);
        ArrayList result = new ArrayList();
        graph.sortStep(result, queue);
        
        assertEquals(1, result.size());
        assertSame(a, result.get(0));
        
        assertEquals(2, queue.size());
        assertSame(b, queue.get(0));
        assertSame(c, queue.get(1));

        assertEquals(0, graph.predecessors(c).size());
        assertEquals(0, graph.predecessors(b).size());
    }

}
