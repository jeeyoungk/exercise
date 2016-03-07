package com.kimjeeyoung.datastruct;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public abstract class AbstractHeapTest {

  @Test
  public void testInsert() {
    Heap<Integer> heap = newHeap();
    heap.insert(1);
    assertEquals(heap.getMinimum().intValue(), 1);
    heap.insert(2);
    assertEquals(heap.getMinimum().intValue(), 1);
    heap.insert(-1);
    assertEquals(heap.getMinimum().intValue(), -1);
    validate(heap);
  }

  @Test
  public void testReverseHeap() {
    Heap<Integer> heap = newHeap((left, right) -> right.compareTo(left));
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
    validate(heap);
  }


  @Test
  public void testInsertPop() {
    Heap<Integer> heap = newHeap();
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
    validate(heap);
  }

  @Test
  public void testRandomOperations() {
    final Random RANDOM = new Random(0);
    final int NUMBER = 100;
    Heap<Integer> heap = newHeap();
    List<Integer> values = new ArrayList<>();
    for (int i = 0; i < NUMBER; i++) {
      values.add(i);
    }
    Collections.shuffle(values, RANDOM);
    for (Integer value : values) {
      heap.insert(value);
    }
    validate(heap);
    for (int i = 0; i < NUMBER; i++) {
      assertEquals(heap.popMinimum().intValue(), i);
    }
    validate(heap);
  }

  /**
   * Create a new heap used for the test.
   */
  abstract <K> Heap<K> newHeap();

  /**
   * Create a new heap used for the test.
   */
  abstract <K> Heap<K> newHeap(Comparator<K> comparator);

  /**
   * validate the given heap.
   *
   * @param heap heap to validate. This heap is produced by {@link #newHeap()} method.
   */
  abstract void validate(Heap<?> heap);
}
