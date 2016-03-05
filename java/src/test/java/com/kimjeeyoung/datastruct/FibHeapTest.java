package com.kimjeeyoung.datastruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    heap.validateState();
  }

  @Test
  public void testReverseHeap() {
    FibHeap<Integer> heap = new FibHeap<>((o1, o2) -> o2.compareTo(o1));
    assertEquals(0, heap.size());
    heap.insert(102);
    assertEquals(102, heap.getMinimum().intValue());
    assertEquals(1, heap.size());
    heap.insert(101);
    assertEquals(102, heap.getMinimum().intValue());
    assertEquals(2, heap.size());
    heap.insert(103);
    assertEquals(103, heap.getMinimum().intValue());
    assertEquals(3, heap.size());
    heap.validateState();
  }

  @Test
  public void testPop_simple() {
    FibHeap<Integer> heap = new FibHeap<>();
    heap.insert(101);
    heap.insert(102);
    assertEquals(101, heap.popMinimum().intValue());
    assertEquals(1, heap.size());
    assertEquals(102, heap.popMinimum().intValue());
    assertEquals(0, heap.size());
    heap.validateState();
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
    assertEquals(2003, heap.popMinimum().intValue());
    assertEquals(2004, heap.popMinimum().intValue());
    heap.validateState();
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
    heapC.validateState();
  }

  @Test
  public void testDecrement_simple() {
    FibHeap<Integer> heap = new FibHeap<>();
    Heap.HeapNode<Integer> last = heap.insert(6);
    Heap.HeapNode<Integer> secondLast = heap.insert(5);
    heap.insert(4);
    heap.insert(3);
    heap.insert(2);
    heap.insert(1);
    assertEquals(1, heap.popMinimum().intValue());
    last.decrement(0);
    secondLast.decrement(-1);
    assertEquals(-1, heap.popMinimum().intValue());
    heap.validateState();
  }

  @Test
  public void testRandom_union() {
    final int SIZE_PER_HEAP = 100;
    final int NUM_HEAPS = 100;
    final Random RANDOM = new Random(0);
    FibHeap<Integer> combinedHeap = new FibHeap<>();
    for (int j = 0; j < NUM_HEAPS; j++) {
      FibHeap<Integer> localHeap = new FibHeap<>();
      List<Integer> values = new ArrayList<>();
      for (int i = 0; i < SIZE_PER_HEAP; i++) {
        values.add(j * SIZE_PER_HEAP + i);
      }
      Collections.shuffle(values, RANDOM);
      for (Integer value : values) {
        localHeap.insert(value);
      }
      combinedHeap = combinedHeap.union(localHeap);
    }
    combinedHeap.validateState();
    assertEquals(SIZE_PER_HEAP * NUM_HEAPS, combinedHeap.size());
    for (int i = 0; i < SIZE_PER_HEAP * NUM_HEAPS; i++) {
      assertEquals(i, combinedHeap.popMinimum().intValue());
      if (i % SIZE_PER_HEAP == 0) {
        combinedHeap.validateState();
      }
    }
    assertEquals(0, combinedHeap.size());
    combinedHeap.validateState();
  }
}