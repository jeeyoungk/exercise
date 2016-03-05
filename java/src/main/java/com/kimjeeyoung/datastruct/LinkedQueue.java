// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.NoSuchElementException;

/**
 * Queue implemented in linked list
 */
public class LinkedQueue<T> implements Queue<T> {
  private static final class Node<T> {
    T value;
    Node<T> next;
  }

  private Node<T> head;
  private Node<T> tail;

  public LinkedQueue() {
  }

  public void add(T value) {
    Node<T> newTail = new Node<>();
    newTail.value = value;
    synchronized (this) {
      if (head == null) {
        head = newTail;
      }
      if (tail != null) {
        tail.next = newTail;
      }
      tail = newTail;
    }
  }

  public T pop() {
    synchronized (this) {
      if (head != null) {
        T value = head.value;
        head = head.next;
        return value;
      } else {
        throw new NoSuchElementException();
      }
    }
  }
}
