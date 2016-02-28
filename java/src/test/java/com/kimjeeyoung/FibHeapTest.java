package com.kimjeeyoung;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by jeeyoungk on 2/27/16.
 */
public class FibHeapTest {
    @Test
    public void testInsert() {
        FibHeap<Integer> heap = new FibHeap<>();
        assertEquals(0, heap.size());
        heap.insert(102);
        assertEquals(102, heap.getMinimum().intValue());
        assertEquals(1, heap.size());
        heap.insert(101);
        assertEquals(101, heap.getMinimum().intValue());
        assertEquals(2, heap.size());
    }

    @Test
    public void testPop() {
        FibHeap<Integer> heap = new FibHeap<>();
        heap.insert(101);
        heap.insert(102);
        assertEquals(101, heap.popMinimum().intValue());
        assertEquals(1, heap.size());
        assertEquals(102, heap.popMinimum().intValue());
        assertEquals(0, heap.size());
        assertNull(heap.popMinimum());
    }
    @Test
    public void testPop_complex() {
        FibHeap<Integer> heap = new FibHeap<>();
        heap.insert(1001);
        heap.insert(2002);
        heap.insert(2003);
        heap.insert(2008);
        heap.insert(2004);
        heap.insert(2005);
        heap.insert(2006);
        heap.insert(2007);
        assertEquals(1001, heap.popMinimum().intValue());
        heap.insert(2008);
        heap.insert(2009);
        heap.insert(2010);
        heap.insert(2011);
        heap.insert(2012);
        assertEquals(2002, heap.popMinimum().intValue());
    }

    @Test
    public void testUnion() {
        FibHeap<Integer> heapA = new FibHeap<>();
        heapA.insert(1);
        FibHeap<Integer> heapB = new FibHeap<>();
        heapB.insert(1);
        heapB.insert(3);
        FibHeap<Integer> heapC = heapA.union(heapB);
        assertEquals(3, heapC.size());
        assertEquals(1, heapC.getMinimum().intValue());
    }
}