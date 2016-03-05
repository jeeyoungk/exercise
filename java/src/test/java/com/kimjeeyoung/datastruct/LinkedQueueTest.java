package com.kimjeeyoung.datastruct;

public class LinkedQueueTest extends AbstractQueueTest{

  @Override
  protected <T> Queue<T> newQueue() {
    return new LinkedQueue<>();
  }

  @Override
  protected <T> Queue<T> newBoundedQueue(int capacity) {
    return null;
  }
}