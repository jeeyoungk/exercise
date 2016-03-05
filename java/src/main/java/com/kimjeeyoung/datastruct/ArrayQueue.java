// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.NoSuchElementException;

/**
 * queue implementation in array.
 */
public class ArrayQueue<T> implements Queue<T> {
  private final T[] data;
  private final int capacity;
  private int size;
  private int begin;
  private int end;

  @SuppressWarnings("unchecked")
  public ArrayQueue(int capacity) {
    this.data = (T[]) new Object[capacity];
    this.capacity = capacity;
    this.begin = capacity - 1;
  }

  @Override
  public void add(T value) throws IllegalStateException {
    synchronized (this) {
      if (size == capacity) {
        throw new IllegalStateException();
      }
      size++;
      data[end] = value;
      end = (end + 1) % capacity;
    }
  }

  @Override
  public T pop() throws NoSuchElementException {
    synchronized (this) {
      if (size == 0) {
        throw new NoSuchElementException();
      }
      size--;
      begin = (begin + 1) % capacity;
      return data[begin];
    }
  }
}
