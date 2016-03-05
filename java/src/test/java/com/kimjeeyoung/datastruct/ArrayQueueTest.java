package com.kimjeeyoung.datastruct;

import static org.junit.Assert.*;

/**
 * Created by jee on 3/5/16.
 */
public class ArrayQueueTest extends AbstractQueueTest {

  @Override
  protected <T> Queue<T> newQueue() {
    return new ArrayQueue<>(1024);
  }

  @Override
  protected <T> Queue<T> newBoundedQueue(int capacity) {
    return new ArrayQueue<>(capacity);
  }
}