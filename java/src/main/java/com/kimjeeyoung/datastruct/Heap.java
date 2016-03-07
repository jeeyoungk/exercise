// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.NoSuchElementException;

/**
 * Lightweight interface for heaps.
 */
public interface Heap<T> {
  interface HeapNode<T> {
    /**
     * Decrement the key of the given node.
     *
     * @param t value decremented to.
     * @throws IllegalArgumentException      if the current value is smaller than <code>t</code>.
     * @throws UnsupportedOperationException if the heap implementation does not support this
     *                                       operaiton.
     */
    void decrement(T t) throws UnsupportedOperationException, IllegalArgumentException;
  }

  /**
   * Insert an element to the heap.
   *
   * @throws IllegalStateException if the heap cannot accept new elements.
   */
  HeapNode insert(T t) throws IllegalStateException;

  /**
   * Get the minimum element from the heap.
   *
   * @throws NoSuchElementException if the heap is empty.
   */
  T getMinimum() throws NoSuchElementException;

  /**
   * Pop the minimum element from the heap.
   *
   * @throws NoSuchElementException if the heap is empty.
   */
  T popMinimum() throws NoSuchElementException;

  /**
   * Returns the number of elements in this heap.
   *
   * @return the number of elements in this heap.
   */
  int size();
}
