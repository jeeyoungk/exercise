// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.NoSuchElementException;

public interface Queue<T> {

  /**
   * Add a new element to the queue.
   *
   * @throws IllegalStateException if the element cannot be added at this time due to capacity
   * restrictions
   */
  void add(T value) throws IllegalStateException;

  /**
   * Pop an element from the queue.
   *
   * @throws NoSuchElementException if this queue is empty.
   */
  T pop() throws NoSuchElementException;
}
